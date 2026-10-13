package vn.edu.ute.capstone.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.TopicApprovalDTO;
import vn.edu.ute.capstone.dto.TopicProposalDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.DepartmentRepository;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.repository.TopicAdvisorRepository;
import vn.edu.ute.capstone.repository.TopicRepository;
import vn.edu.ute.capstone.repository.UserRepository;
import vn.edu.ute.capstone.service.TopicService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository topicAdvisorRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Topic createTopic(TopicProposalDTO dto, User currentLecturer) {
        log.info("Lecturer {} is proposing new topic with code: {}", currentLecturer.getUsername(), dto.getCode());

        // Guard 1: Đợt đăng ký có hợp lệ và đang mở cho GV không?
        RegistrationPeriod period = validatePeriodForProposal(dto.getPeriodId());

        // Guard 2: Bộ môn có tồn tại không?
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ môn tương ứng."));

        // Guard 3: Kiểm tra tính duy nhất của mã đề tài
        if (topicRepository.existsByCode(dto.getCode().trim())) {
            throw new IllegalArgumentException("Mã đề tài '" + dto.getCode() + "' đã tồn tại trên hệ thống.");
        }

        // Guard 4: Kiểm tra GV đồng hướng dẫn (nếu có)
        User coLecturer = validateCoLecturer(currentLecturer, dto.getCoLecturerId());

        // Build Topic
        Topic topic = Topic.builder()
                .code(dto.getCode().trim().toUpperCase())
                .title(dto.getTitle().trim())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .period(period)
                .department(department)
                .status(TopicStatus.PENDING_DEPT)
                .build();

        // Thêm danh sách GVHD vào topic (Topic cascade = ALL)
        List<TopicAdvisor> advisors = new ArrayList<>();
        advisors.add(TopicAdvisor.builder()
                .id(new TopicAdvisorId(null, currentLecturer.getId()))
                .topic(topic)
                .lecturer(currentLecturer)
                .isPrimary(true)
                .build());

        if (coLecturer != null) {
            advisors.add(TopicAdvisor.builder()
                    .id(new TopicAdvisorId(null, coLecturer.getId()))
                    .topic(topic)
                    .lecturer(coLecturer)
                    .isPrimary(false)
                    .build());
        }

        topic.setAdvisors(advisors);
        return topicRepository.save(topic);
    }

    @Override
    @Transactional
    public Topic updateTopic(Long topicId, TopicProposalDTO dto, User currentLecturer) {
        Topic topic = findById(topicId);

        // Guard 1: Quyền tác giả - Chỉ GV trong ban hướng dẫn mới được sửa đề tài
        boolean isAdvisor = topicAdvisorRepository.existsByTopicIdAndLecturerId(topicId, currentLecturer.getId());
        if (!isAdvisor) {
            throw new IllegalStateException("Bạn không có quyền chỉnh sửa đề tài này vì không nằm trong danh sách giảng viên hướng dẫn.");
        }

        // Guard 2: Trạng thái đề tài - Chỉ được sửa khi đang chờ duyệt hoặc bị từ chối
        if (topic.getStatus() != TopicStatus.PENDING_DEPT && topic.getStatus() != TopicStatus.REJECTED) {
            throw new IllegalStateException("Đề tài đã ở trạng thái '" + topic.getStatus().getDisplayName() + "', không thể chỉnh sửa.");
        }

        // Guard 3: Đợt đăng ký có đang cho phép sửa/đăng ký không?
        RegistrationPeriod period = validatePeriodForProposal(dto.getPeriodId());

        // Guard 4: Bộ môn
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ môn tương ứng."));

        // Guard 5: Trùng mã đề tài với đề tài khác
        if (topicRepository.existsByCodeAndIdNot(dto.getCode().trim(), topicId)) {
            throw new IllegalArgumentException("Mã đề tài '" + dto.getCode() + "' đã được sử dụng bởi đề tài khác.");
        }

        // Guard 6: Đồng hướng dẫn
        User coLecturer = validateCoLecturer(currentLecturer, dto.getCoLecturerId());

        topic.setCode(dto.getCode().trim().toUpperCase());
        topic.setTitle(dto.getTitle().trim());
        topic.setDescription(dto.getDescription());
        topic.setRequirements(dto.getRequirements());
        topic.setPeriod(period);
        topic.setDepartment(department);
        topic.setStatus(TopicStatus.PENDING_DEPT); // Reset về chờ duyệt khi có cập nhật
        topic.setRejection_reason(null);

        // Cập nhật lại danh sách TopicAdvisors
        topic.getAdvisors().clear();

        topic.getAdvisors().add(TopicAdvisor.builder()
                .id(new TopicAdvisorId(topic.getId(), currentLecturer.getId()))
                .topic(topic)
                .lecturer(currentLecturer)
                .isPrimary(true)
                .build());

        if (coLecturer != null) {
            topic.getAdvisors().add(TopicAdvisor.builder()
                    .id(new TopicAdvisorId(topic.getId(), coLecturer.getId()))
                    .topic(topic)
                    .lecturer(coLecturer)
                    .isPrimary(false)
                    .build());
        }

        return topicRepository.save(topic);
    }

    @Override
    @Transactional
    public void deleteTopic(Long topicId, User currentLecturer) {
        Topic topic = findById(topicId);

        // Guard 1: Phải là GVHD
        boolean isAdvisor = topicAdvisorRepository.existsByTopicIdAndLecturerId(topicId, currentLecturer.getId());
        if (!isAdvisor) {
            throw new IllegalStateException("Bạn không có quyền xóa đề tài này.");
        }

        // Guard 2: Chỉ được xóa khi chưa duyệt hoặc bị từ chối
        if (topic.getStatus() != TopicStatus.PENDING_DEPT && topic.getStatus() != TopicStatus.REJECTED) {
            throw new IllegalStateException("Không thể xóa đề tài đang ở trạng thái '" + topic.getStatus().getDisplayName() + "'.");
        }

        topicRepository.delete(topic);
    }

    @Override
    @Transactional
    public Topic approveOrRejectTopic(TopicApprovalDTO dto, User approver) {
        Topic topic = findById(dto.getTopicId());

        // Guard 1: Đề tài phải ở trạng thái PENDING_DEPT mới được duyệt/từ chối
        if (topic.getStatus() != TopicStatus.PENDING_DEPT) {
            throw new IllegalStateException("Chỉ có thể xét duyệt các đề tài đang ở trạng thái 'Chờ bộ môn duyệt'.");
        }

        // Guard 2: Phân quyền duyệt
        // Nếu là Trưởng khoa (ADMIN/DEAN) -> duyệt được tất cả.
        // Nếu là Trưởng bộ môn (HEAD_OF_DEPARTMENT) -> chỉ duyệt được đề tài thuộc bộ môn của mình.
        boolean isAdmin = approver.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            if (approver.getDepartment() == null || !approver.getDepartment().getId().equals(topic.getDepartment().getId())) {
                throw new IllegalStateException("Bạn chỉ có thẩm quyền xét duyệt các đề tài thuộc bộ môn của bạn.");
            }
        }

        // Guard 3: Nếu từ chối, bắt buộc phải có lý do
        if (Boolean.FALSE.equals(dto.getApproved())) {
            if (dto.getRejectionReason() == null || dto.getRejectionReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập lý do từ chối đề tài để giảng viên có cơ sở chỉnh sửa.");
            }
            topic.setStatus(TopicStatus.REJECTED);
            topic.setRejection_reason(dto.getRejectionReason().trim());
        } else {
            topic.setStatus(TopicStatus.APPROVED);
            topic.setRejection_reason(null);
        }

        return topicRepository.save(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public Topic findById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> getTopicsForLecturer(Long lecturerId, Long periodId) {
        if (periodId != null && periodId > 0) {
            return topicRepository.findByLecturerIdAndPeriodId(lecturerId, periodId);
        }
        return topicRepository.findByLecturerId(lecturerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> getTopicsForDepartment(Long departmentId, Long periodId) {
        if (periodId != null && periodId > 0) {
            return topicRepository.findByDepartmentIdAndPeriodId(departmentId, periodId);
        }
        return topicRepository.findByDepartmentId(departmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> getAllTopics(Long periodId) {
        if (periodId != null && periodId > 0) {
            return topicRepository.findByPeriodId(periodId);
        }
        return topicRepository.findAll();
    }

    private RegistrationPeriod validatePeriodForProposal(Long periodId) {
        RegistrationPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký tương ứng."));

        if (period.getStatus() != PeriodStatus.OPEN_LECTURER) {
            throw new IllegalStateException("Đợt đăng ký '" + period.getName() + "' hiện không mở cho giảng viên đề xuất đề tài.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(period.getLecturerStartDate()) || now.isAfter(period.getLecturerEndDate())) {
            throw new IllegalStateException("Thời gian đề xuất đề tài cho đợt này đã kết thúc hoặc chưa bắt đầu (Từ " 
                    + period.getLecturerStartDate() + " đến " + period.getLecturerEndDate() + ").");
        }

        return period;
    }

    private User validateCoLecturer(User currentLecturer, Long coLecturerId) {
        if (coLecturerId == null || coLecturerId <= 0) {
            return null;
        }

        if (coLecturerId.equals(currentLecturer.getId())) {
            throw new IllegalArgumentException("Bạn không thể chọn chính mình làm Giảng viên đồng hướng dẫn.");
        }

        return userRepository.findById(coLecturerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Giảng viên đồng hướng dẫn được chọn."));
    }
}
