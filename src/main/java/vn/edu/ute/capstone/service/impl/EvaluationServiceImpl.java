package vn.edu.ute.capstone.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.CouncilCreateDTO;
import vn.edu.ute.capstone.dto.EvaluationDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.*;
import vn.edu.ute.capstone.service.EvaluationService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final EvaluationRepository evaluationRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository topicAdvisorRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Council createCouncil(CouncilCreateDTO dto, User admin) {
        log.info("Creating council '{}' for period {}", dto.getName(), dto.getPeriodId());

        RegistrationPeriod period = periodRepository.findById(dto.getPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký."));

        // Guard 1: Đảm bảo Chủ tịch và Thư ký không được trùng nhau
        if (dto.getChairmanId().equals(dto.getSecretaryId())) {
            throw new IllegalArgumentException("Chủ tịch hội đồng và Thư ký hội đồng không được là cùng một người.");
        }

        // Guard 2: Tổng số thành viên từ 3 đến 5 GV (Quy chế đề án)
        Set<Long> uniqueLecturerIds = new HashSet<>();
        uniqueLecturerIds.add(dto.getChairmanId());
        uniqueLecturerIds.add(dto.getSecretaryId());
        if (dto.getMemberIds() != null) {
            uniqueLecturerIds.addAll(dto.getMemberIds());
        }

        if (uniqueLecturerIds.size() < 3 || uniqueLecturerIds.size() > 5) {
            throw new IllegalArgumentException("Hội đồng phản biện phải có từ 3 đến 5 giảng viên (Hiện tại: " + uniqueLecturerIds.size() + " người).");
        }

        Council council = Council.builder()
                .name(dto.getName().trim())
                .period(period)
                .defenseDate(dto.getDefenseDate())
                .location(dto.getLocation())
                .build();

        // Gán các đề tài được phân công chấm
        if (dto.getAssignedTopicIds() != null && !dto.getAssignedTopicIds().isEmpty()) {
            List<Topic> topics = topicRepository.findAllById(dto.getAssignedTopicIds());
            
            // Guard 3: Kiểm tra xung đột lợi ích: Giảng viên hướng dẫn KHÔNG ĐƯỢC nằm trong hội đồng chấm đề tài của mình!
            for (Topic t : topics) {
                List<TopicAdvisor> advisors = topicAdvisorRepository.findByTopicId(t.getId());
                for (TopicAdvisor adv : advisors) {
                    if (uniqueLecturerIds.contains(adv.getLecturer().getId())) {
                        throw new IllegalStateException("Xung đột quyền lợi: Giảng viên '" + adv.getLecturer().getFullName() 
                                + "' là GVHD của đề tài '" + t.getCode() + "', không được phép tham gia hội đồng chấm đề tài này!");
                    }
                }
            }
            council.setAssignedTopics(new HashSet<>(topics));
        }

        // Chuẩn bị danh sách thành viên hội đồng
        List<CouncilMember> members = new ArrayList<>();

        // Chủ tịch
        User chairman = userRepository.findById(dto.getChairmanId()).orElseThrow();
        members.add(CouncilMember.builder()
                .id(new CouncilMemberId(null, chairman.getId()))
                .council(council)
                .lecturer(chairman)
                .roleInCouncil(CouncilRole.CHAIRMAN)
                .build());

        // Thư ký
        User secretary = userRepository.findById(dto.getSecretaryId()).orElseThrow();
        members.add(CouncilMember.builder()
                .id(new CouncilMemberId(null, secretary.getId()))
                .council(council)
                .lecturer(secretary)
                .roleInCouncil(CouncilRole.SECRETARY)
                .build());

        // Các ủy viên còn lại
        if (dto.getMemberIds() != null) {
            for (Long memId : dto.getMemberIds()) {
                if (!memId.equals(dto.getChairmanId()) && !memId.equals(dto.getSecretaryId())) {
                    User member = userRepository.findById(memId).orElseThrow();
                    members.add(CouncilMember.builder()
                            .id(new CouncilMemberId(null, member.getId()))
                            .council(council)
                            .lecturer(member)
                            .roleInCouncil(CouncilRole.MEMBER)
                            .build());
                }
            }
        }

        council.setMembers(members);
        return councilRepository.save(council);
    }

    @Override
    @Transactional
    public Council updateCouncil(Long councilId, CouncilCreateDTO dto, User admin) {
        deleteCouncil(councilId, admin);
        dto.setId(null);
        return createCouncil(dto, admin);
    }

    @Override
    @Transactional
    public void deleteCouncil(Long councilId, User admin) {
        Council council = findCouncilById(councilId);
        councilMemberRepository.deleteAll(council.getMembers());
        councilRepository.delete(council);
    }

    @Override
    @Transactional
    public Evaluation submitEvaluation(EvaluationDTO dto, User evaluator) {
        log.info("Lecturer {} is submitting score {} for topic {} as {}", evaluator.getUsername(), dto.getScore(), dto.getTopicId(), dto.getEvaluationType());

        Topic topic = topicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài."));

        boolean isAdvisor = topicAdvisorRepository.existsByTopicIdAndLecturerId(topic.getId(), evaluator.getId());

        // Guard 1: Phân định quyền chấm theo loại đánh giá
        if (dto.getEvaluationType() == EvaluationType.ADVISOR) {
            // Chấm điểm hướng dẫn: Bắt buộc PHẢI là GVHD của đề tài
            if (!isAdvisor) {
                throw new IllegalStateException("Bạn không phải là giảng viên hướng dẫn của đề tài này nên không thể chấm điểm hướng dẫn.");
            }
        } else {
            // Chấm phản biện hoặc chấm hội đồng: TUYỆT ĐỐI KHÔNG ĐƯỢC là GVHD (Quy chế liêm chính)
            if (isAdvisor) {
                throw new IllegalStateException("Quy chế liêm chính: Giảng viên hướng dẫn không được phép tham gia chấm phản biện hoặc hội đồng cho đề tài của mình.");
            }
        }

        // Guard 2: Điểm số hợp lệ từ 0.0 đến 10.0
        if (dto.getScore() == null || dto.getScore().doubleValue() < 0.0 || dto.getScore().doubleValue() > 10.0) {
            throw new IllegalArgumentException("Điểm số phải nằm trong thang điểm từ 0.00 đến 10.00.");
        }

        Council council = null;
        if (dto.getEvaluationType() == EvaluationType.COUNCIL) {
            if (dto.getCouncilId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn Hội đồng khi chấm điểm hội đồng.");
            }
            council = findCouncilById(dto.getCouncilId());

            // Guard 3: Nếu là chấm hội đồng, GV đó phải là thành viên của Hội đồng
            boolean isMemberOfCouncil = councilMemberRepository.existsByCouncilIdAndLecturerId(council.getId(), evaluator.getId());
            if (!isMemberOfCouncil) {
                throw new IllegalStateException("Bạn không phải là thành viên trong hội đồng này.");
            }
        }

        // Cập nhật hoặc tạo mới điểm số
        Optional<Evaluation> existingEval = evaluationRepository.findByTopicIdAndEvaluatorIdAndEvaluationType(
                topic.getId(), evaluator.getId(), dto.getEvaluationType()
        );

        Evaluation evaluation;
        if (existingEval.isPresent()) {
            evaluation = existingEval.get();
            evaluation.setScore(dto.getScore());
            evaluation.setComments(dto.getComments());
            evaluation.setCouncil(council);
        } else {
            evaluation = Evaluation.builder()
                    .topic(topic)
                    .council(council)
                    .evaluator(evaluator)
                    .score(dto.getScore())
                    .comments(dto.getComments())
                    .evaluationType(dto.getEvaluationType())
                    .build();
        }

        return evaluationRepository.save(evaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Council> getCouncilsByPeriod(Long periodId) {
        if (periodId != null && periodId > 0) {
            return councilRepository.findByPeriodId(periodId);
        }
        return councilRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Council> getCouncilsByLecturer(Long lecturerId) {
        return councilRepository.findByLecturerId(lecturerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Evaluation> getEvaluationsByTopic(Long topicId, User currentUser) {
        return evaluationRepository.findByTopicId(topicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Evaluation> getEvaluationsByEvaluator(Long evaluatorId) {
        return evaluationRepository.findByEvaluatorIdOrderByCreatedAtDesc(evaluatorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageScore(Long topicId) {
        Double avg = evaluationRepository.calculateAverageScoreByTopicId(topicId);
        return (avg != null) ? Math.round(avg * 100.0) / 100.0 : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Council findCouncilById(Long councilId) {
        return councilRepository.findById(councilId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hội đồng với ID: " + councilId));
    }
}
