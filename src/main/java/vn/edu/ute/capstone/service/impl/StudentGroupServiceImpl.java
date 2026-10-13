package vn.edu.ute.capstone.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.CreateGroupDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.*;
import vn.edu.ute.capstone.service.StudentGroupService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentGroupServiceImpl implements StudentGroupService {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final vn.edu.ute.capstone.service.RegistrationPeriodService periodService;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StudentGroup createGroup(CreateGroupDTO dto, User leader) {
        log.info("Student {} is creating group with code: {} in period {}", leader.getUsername(), dto.getGroupCode(), dto.getPeriodId());

        // Guard 1: Đợt đăng ký có tồn tại và đang mở không?
        RegistrationPeriod period = validatePeriodForStudent(dto.getPeriodId());

        // Guard 1.5: Kiểm tra điều kiện tiên quyết của sinh viên (Tín chỉ, GPA, Khóa K, Khoa)
        vn.edu.ute.capstone.dto.StudentEligibilityDTO eligibility = periodService.checkStudentEligibility(leader, period);
        if (!eligibility.isEligible()) {
            throw new IllegalStateException("Bạn không đủ điều kiện tham gia đợt này: " + String.join("; ", eligibility.getReasons()));
        }

        // Guard 2: Sinh viên đã tham gia nhóm nào trong đợt này chưa? (Luật 1 SV - 1 Nhóm/Đợt)
        if (groupMemberRepository.existsByStudentIdAndPeriodId(leader.getId(), period.getId())) {
            throw new IllegalStateException("Bạn đã tham gia một nhóm khác trong đợt đăng ký này. Mỗi sinh viên chỉ được thuộc duy nhất 1 nhóm.");
        }

        // Guard 3: Mã nhóm có bị trùng không?
        String code = dto.getGroupCode().trim().toUpperCase();
        if (groupRepository.existsByGroupCode(code)) {
            throw new IllegalArgumentException("Mã nhóm '" + code + "' đã tồn tại trên hệ thống. Vui lòng chọn mã khác.");
        }

        // Tạo nhóm
        StudentGroup group = StudentGroup.builder()
                .groupCode(code)
                .period(period)
                .status(GroupStatus.REGISTERING)
                .build();
        StudentGroup savedGroup = groupRepository.save(group);

        // Tạo thành viên Trưởng nhóm
        GroupMember leaderMember = GroupMember.builder()
                .group(savedGroup)
                .student(leader)
                .period(period)
                .isLeader(true)
                .build();
        groupMemberRepository.save(leaderMember);

        savedGroup.setMembers(new ArrayList<>(List.of(leaderMember)));
        return savedGroup;
    }

    @Override
    @Transactional
    public StudentGroup addMember(Long groupId, String studentUsername, User currentLeader) {
        // Khóa nhóm bằng Pessimistic Lock để chống race condition thêm vượt quá 3 thành viên
        StudentGroup group = groupRepository.findByIdForUpdate(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm sinh viên với ID: " + groupId));

        // Guard 1: Chỉ nhóm trưởng mới được thêm thành viên
        validateLeaderAuthorization(group, currentLeader);

        // Guard 2: Nhóm đã đủ tối đa 3 thành viên chưa?
        long memberCount = groupMemberRepository.countByGroupId(groupId);
        if (memberCount >= 3) {
            throw new IllegalStateException("Nhóm đã đạt số lượng tối đa 3 thành viên. Không thể thêm thành viên mới.");
        }

        // Guard 3: Sinh viên cần thêm có tồn tại trên hệ thống không?
        User newStudent = userRepository.findByUsername(studentUsername.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên với MSSV/Tài khoản: " + studentUsername));

        // Kiểm tra role STUDENT
        boolean isStudent = newStudent.getRoles().stream().anyMatch(r -> "ROLE_STUDENT".equals(r.getName()));
        if (!isStudent) {
            throw new IllegalArgumentException("Tài khoản '" + studentUsername + "' không phải là Sinh viên.");
        }

        // Guard 4: Sinh viên đó đã ở trong nhóm nào trong đợt này chưa?
        if (groupMemberRepository.existsByStudentIdAndPeriodId(newStudent.getId(), group.getPeriod().getId())) {
            throw new IllegalStateException("Sinh viên " + newStudent.getFullName() + " (" + studentUsername + ") đã tham gia một nhóm khác trong đợt này.");
        }

        // Guard 4.5: Kiểm tra điều kiện tiên quyết của sinh viên mới (Tín chỉ, GPA, Khóa K, Khoa)
        vn.edu.ute.capstone.dto.StudentEligibilityDTO memberEligibility = periodService.checkStudentEligibility(newStudent, group.getPeriod());
        if (!memberEligibility.isEligible()) {
            throw new IllegalStateException("Sinh viên " + newStudent.getFullName() + " (" + studentUsername + ") không đủ điều kiện tham gia đợt này: " + String.join("; ", memberEligibility.getReasons()));
        }

        // Guard 5: Không cho thêm bớt khi nhóm đã đăng ký đề tài
        if (group.getTopic() != null) {
            throw new IllegalStateException("Nhóm đã đăng ký đề tài thành công, danh sách thành viên hiện đã bị khóa.");
        }

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .student(newStudent)
                .period(group.getPeriod())
                .isLeader(false)
                .build();
        groupMemberRepository.save(newMember);

        group.getMembers().add(newMember);
        return group;
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long memberId, User currentLeader) {
        StudentGroup group = findById(groupId);

        // Guard 1: Chỉ nhóm trưởng mới được xóa thành viên
        validateLeaderAuthorization(group, currentLeader);

        // Guard 2: Không được tự xóa chính mình (nhóm trưởng)
        GroupMember memberToRemove = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên trong nhóm."));

        if (!memberToRemove.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Thành viên không thuộc nhóm này.");
        }

        if (Boolean.TRUE.equals(memberToRemove.getIsLeader())) {
            throw new IllegalStateException("Nhóm trưởng không thể tự xóa chính mình khỏi nhóm. Vui lòng chọn 'Giải tán nhóm' nếu muốn hủy.");
        }

        // Guard 3: Khóa khi đã có đề tài
        if (group.getTopic() != null) {
            throw new IllegalStateException("Không thể xóa thành viên khi nhóm đã đăng ký đề tài.");
        }

        group.getMembers().remove(memberToRemove);
        groupMemberRepository.delete(memberToRemove);
    }

    @Override
    @Transactional
    public void disbandGroup(Long groupId, User currentLeader) {
        StudentGroup group = findById(groupId);

        // Guard 1: Chỉ nhóm trưởng mới được giải tán nhóm
        validateLeaderAuthorization(group, currentLeader);

        // Nếu nhóm đang giữ đề tài -> Phải trả lại đề tài về trạng thái APPROVED trước khi giải tán
        if (group.getTopic() != null) {
            Topic topic = group.getTopic();
            topic.setStatus(TopicStatus.APPROVED);
            topicRepository.save(topic);
            group.setTopic(null);
        }

        groupMemberRepository.deleteAll(group.getMembers());
        groupRepository.delete(group);
    }

    @Override
    @Transactional
    public StudentGroup registerTopic(Long groupId, Long topicId, User currentLeader) {
        StudentGroup group = findById(groupId);

        // Guard 1: Quyền nhóm trưởng
        validateLeaderAuthorization(group, currentLeader);

        // Guard 2: Đợt đăng ký có đang mở cổng cho SV không?
        validatePeriodForStudent(group.getPeriod().getId());

        // Guard 3: Nhóm đã đăng ký đề tài nào chưa? (Mỗi nhóm chỉ 1 đề tài duy nhất)
        if (group.getTopic() != null) {
            throw new IllegalStateException("Nhóm của bạn đã đăng ký đề tài '" + group.getTopic().getTitle() + "'. Vui lòng hủy đề tài cũ trước nếu muốn đổi đề tài khác.");
        }

        // Guard 4: Đề tài có tồn tại không?
        // Sử dụng Khóa bi quan (Pessimistic Lock) để chống xung đột race condition khi nhiều nhóm cùng đăng ký
        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài tương ứng."));

        // Guard 5: Đề tài có thuộc cùng đợt với nhóm không?
        if (!topic.getPeriod().getId().equals(group.getPeriod().getId())) {
            throw new IllegalArgumentException("Đề tài không thuộc đợt đăng ký của nhóm bạn.");
        }

        // Guard 6: Đề tài đã được Bộ môn phê duyệt chưa?
        if (topic.getStatus() != TopicStatus.APPROVED) {
            throw new IllegalStateException("Đề tài này hiện chưa sẵn sàng để đăng ký (Trạng thái: " + topic.getStatus().getDisplayName() + ").");
        }

        // Guard 7: Đề tài đã có nhóm khác nhanh tay nhận trước chưa? (Race Condition Guard)
        if (groupRepository.existsByTopicId(topicId)) {
            throw new IllegalStateException("Đề tài này vừa được nhóm khác đăng ký trước. Vui lòng chọn đề tài khác.");
        }

        // Tiến hành gán đề tài và cập nhật trạng thái
        group.setTopic(topic);
        group.setStatus(GroupStatus.ACCEPTED);
        topic.setStatus(TopicStatus.ASSIGNED);

        topicRepository.save(topic);
        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public void cancelTopicRegistration(Long groupId, User currentLeader) {
        StudentGroup group = findById(groupId);

        // Guard 1: Quyền nhóm trưởng
        validateLeaderAuthorization(group, currentLeader);

        // Guard 2: Đợt đăng ký có còn mở không?
        validatePeriodForStudent(group.getPeriod().getId());

        if (group.getTopic() == null) {
            throw new IllegalStateException("Nhóm chưa đăng ký đề tài nào để hủy.");
        }

        Topic topic = group.getTopic();
        topic.setStatus(TopicStatus.APPROVED); // Trả lại đề tài cho nhóm khác đăng ký
        topicRepository.save(topic);

        group.setTopic(null);
        group.setStatus(GroupStatus.REGISTERING);
        groupRepository.save(group);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentGroup> getGroupByStudentAndPeriod(Long studentId, Long periodId) {
        return groupMemberRepository.findByStudentIdAndPeriodId(studentId, periodId)
                .map(GroupMember::getGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentGroup> getGroupsByStudent(Long studentId) {
        return groupMemberRepository.findAllByStudentId(studentId).stream()
                .map(GroupMember::getGroup)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentGroup findById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm với ID: " + groupId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> getAvailableTopicsForStudent(Long periodId) {
        if (periodId != null && periodId > 0) {
            return topicRepository.findByPeriodId(periodId);
        }
        return topicRepository.findAll();
    }

    private void validateLeaderAuthorization(StudentGroup group, User currentLeader) {
        boolean isLeader = group.getMembers().stream()
                .anyMatch(m -> m.getStudent().getId().equals(currentLeader.getId()) && Boolean.TRUE.equals(m.getIsLeader()));
        if (!isLeader) {
            throw new IllegalStateException("Chỉ Nhóm trưởng mới có thẩm quyền thực hiện thao tác này.");
        }
    }

    private RegistrationPeriod validatePeriodForStudent(Long periodId) {
        RegistrationPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký."));

        if (period.getStatus() != PeriodStatus.OPEN_STUDENT) {
            throw new IllegalStateException("Đợt đăng ký '" + period.getName() + "' hiện không mở cổng cho sinh viên (Trạng thái: " + period.getStatus().getDisplayName() + ").");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(period.getStudentStartDate()) || now.isAfter(period.getStudentEndDate())) {
            throw new IllegalStateException("Thời gian đăng ký cho sinh viên trong đợt này đã kết thúc hoặc chưa bắt đầu.");
        }

        return period;
    }
}
