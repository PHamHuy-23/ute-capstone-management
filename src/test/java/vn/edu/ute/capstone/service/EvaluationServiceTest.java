package vn.edu.ute.capstone.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.CouncilCreateDTO;
import vn.edu.ute.capstone.dto.EvaluationDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EvaluationServiceTest {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private RegistrationPeriodRepository periodRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TopicAdvisorRepository topicAdvisorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User admin;
    private User gvhd;
    private User gv1;
    private User gv2;
    private User gv3;
    private RegistrationPeriod period;
    private Topic topic;

    @BeforeEach
    void setUp() {
        Role roleLecturer = roleRepository.save(Role.builder().name("ROLE_LECTURER").build());
        Role roleAdmin = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        Department dept = departmentRepository.save(Department.builder().name("CNPM").code("SE").build());

        admin = userRepository.save(User.builder()
                .username("dean_admin")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("Trưởng Khoa")
                .email("dean@ute.edu.vn")
                .roles(java.util.Set.of(roleAdmin))
                .isActive(true)
                .build());

        gvhd = userRepository.save(User.builder()
                .username("gv_huongdan")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("ThS. Giảng Viên Hướng Dẫn")
                .email("gvhd@ute.edu.vn")
                .roles(java.util.Set.of(roleLecturer))
                .isActive(true)
                .build());

        gv1 = userRepository.save(User.builder()
                .username("gv_chutich")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("PGS.TS. Chủ Tịch Hội Đồng")
                .email("chutich@ute.edu.vn")
                .roles(java.util.Set.of(roleLecturer))
                .isActive(true)
                .build());

        gv2 = userRepository.save(User.builder()
                .username("gv_thuky")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("TS. Thư Ký Hội Đồng")
                .email("thuky@ute.edu.vn")
                .roles(java.util.Set.of(roleLecturer))
                .isActive(true)
                .build());

        gv3 = userRepository.save(User.builder()
                .username("gv_uyvien")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("ThS. Ủy Viên Hội Đồng")
                .email("uyvien@ute.edu.vn")
                .roles(java.util.Set.of(roleLecturer))
                .isActive(true)
                .build());

        period = periodRepository.save(RegistrationPeriod.builder()
                .name("Đợt KLTN Hội Đồng")
                .periodType(PeriodType.CAPSTONE)
                .status(PeriodStatus.DEFENSE)
                .lecturerStartDate(LocalDateTime.now().minusDays(30))
                .lecturerEndDate(LocalDateTime.now().minusDays(20))
                .studentStartDate(LocalDateTime.now().minusDays(19))
                .studentEndDate(LocalDateTime.now().minusDays(10))
                .defenseDate(LocalDateTime.now().plusDays(2))
                .build());

        topic = topicRepository.save(Topic.builder()
                .code("TOPIC-COUNCIL-01")
                .title("Nghiên cứu kiến trúc Microservices")
                .department(dept)
                .period(period)
                .status(TopicStatus.ASSIGNED)
                .build());

        topicAdvisorRepository.save(TopicAdvisor.builder()
                .id(new TopicAdvisorId(topic.getId(), gvhd.getId()))
                .topic(topic)
                .lecturer(gvhd)
                .isPrimary(true)
                .build());
    }

    @Test
    @DisplayName("Thành lập hội đồng thành công với 3 giảng viên (Chủ tịch, Thư ký, Ủy viên)")
    void testCreateCouncil_Success() {
        CouncilCreateDTO dto = CouncilCreateDTO.builder()
                .name("Hội đồng KLTN 01")
                .periodId(period.getId())
                .chairmanId(gv1.getId())
                .secretaryId(gv2.getId())
                .memberIds(List.of(gv3.getId()))
                .assignedTopicIds(List.of(topic.getId()))
                .build();

        Council council = evaluationService.createCouncil(dto, admin);

        assertNotNull(council.getId());
        assertEquals("Hội đồng KLTN 01", council.getName());
        assertEquals(3, council.getMembers().size());
        assertEquals(1, council.getAssignedTopics().size());
    }

    @Test
    @DisplayName("Chặn thành lập hội đồng khi Chủ tịch và Thư ký trùng nhau")
    void testCreateCouncil_FailsWhenChairmanAndSecretaryDuplicate() {
        CouncilCreateDTO dto = CouncilCreateDTO.builder()
                .name("Hội đồng lỗi")
                .periodId(period.getId())
                .chairmanId(gv1.getId())
                .secretaryId(gv1.getId()) // Trùng Chủ tịch
                .memberIds(List.of(gv3.getId()))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.createCouncil(dto, admin);
        });

        assertTrue(ex.getMessage().contains("không được là cùng một người"));
    }

    @Test
    @DisplayName("Chặn thành lập hội đồng nếu có Giảng viên hướng dẫn của đề tài đó (Xung đột lợi ích)")
    void testCreateCouncil_FailsWhenAdvisorInCouncilForOwnTopic() {
        CouncilCreateDTO dto = CouncilCreateDTO.builder()
                .name("Hội đồng vi phạm liêm chính")
                .periodId(period.getId())
                .chairmanId(gv1.getId())
                .secretaryId(gv2.getId())
                .memberIds(List.of(gvhd.getId())) // Cố tình đưa GVHD vào hội đồng
                .assignedTopicIds(List.of(topic.getId()))
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            evaluationService.createCouncil(dto, admin);
        });

        assertTrue(ex.getMessage().contains("Xung đột quyền lợi"));
    }

    @Test
    @DisplayName("Chặn Giảng viên hướng dẫn chấm phản biện đề tài của mình")
    void testSubmitEvaluation_FailsWhenAdvisorEvaluatesOwnTopic() {
        EvaluationDTO dto = EvaluationDTO.builder()
                .topicId(topic.getId())
                .score(new BigDecimal("9.5"))
                .evaluationType(EvaluationType.REVIEWER)
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            evaluationService.submitEvaluation(dto, gvhd);
        });

        assertTrue(ex.getMessage().contains("Giảng viên hướng dẫn không được phép tham gia chấm phản biện"));
    }

    @Test
    @DisplayName("Giảng viên hướng dẫn chấm điểm hướng dẫn (ADVISOR) thành công")
    void testSubmitEvaluation_AdvisorSuccess() {
        EvaluationDTO dto = EvaluationDTO.builder()
                .topicId(topic.getId())
                .score(new BigDecimal("9.0"))
                .evaluationType(EvaluationType.ADVISOR)
                .comments("Nhóm hoàn thành tốt tiến độ.")
                .build();

        Evaluation eval = evaluationService.submitEvaluation(dto, gvhd);
        assertNotNull(eval.getId());
        assertEquals(EvaluationType.ADVISOR, eval.getEvaluationType());
    }

    @Test
    @DisplayName("Chấm điểm thành công và tính trung bình cộng chính xác")
    void testSubmitEvaluation_SuccessAndCalculateAverage() {
        // Tạo hội đồng trước
        Council council = evaluationService.createCouncil(CouncilCreateDTO.builder()
                .name("Hội đồng KLTN Tính Điểm")
                .periodId(period.getId())
                .chairmanId(gv1.getId())
                .secretaryId(gv2.getId())
                .memberIds(List.of(gv3.getId()))
                .assignedTopicIds(List.of(topic.getId()))
                .build(), admin);

        // GV 1 chấm 8.0 (Phản biện)
        evaluationService.submitEvaluation(EvaluationDTO.builder()
                .topicId(topic.getId())
                .score(new BigDecimal("8.00"))
                .evaluationType(EvaluationType.REVIEWER)
                .build(), gv1);

        // GV 2 chấm 9.0 (Hội đồng)
        evaluationService.submitEvaluation(EvaluationDTO.builder()
                .topicId(topic.getId())
                .councilId(council.getId())
                .score(new BigDecimal("9.00"))
                .evaluationType(EvaluationType.COUNCIL)
                .build(), gv2);

        Double avgScore = evaluationService.getAverageScore(topic.getId());
        assertNotNull(avgScore);
        assertEquals(8.50, avgScore);
    }
}
