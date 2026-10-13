package vn.edu.ute.capstone.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.CreateGroupDTO;
import vn.edu.ute.capstone.dto.SubmissionDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SubmissionServiceTest {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private StudentGroupService studentGroupService;

    @Autowired
    private RegistrationPeriodRepository periodRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User studentLeader;
    private User studentMember;
    private User otherStudent;
    private StudentGroup groupWithTopic;
    private StudentGroup groupWithoutTopic;

    @BeforeEach
    void setUp() {
        Role roleStudent = roleRepository.save(Role.builder().name("ROLE_STUDENT").build());
        Department dept = departmentRepository.save(Department.builder().name("CNPM").code("SE").build());

        studentLeader = userRepository.save(User.builder()
                .username("sv_leader")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("Nhóm Trưởng")
                .email("leader@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleStudent))
                .isActive(true)
                .build());

        studentMember = userRepository.save(User.builder()
                .username("sv_member")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("Thành Viên Nhóm")
                .email("member@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleStudent))
                .isActive(true)
                .build());

        otherStudent = userRepository.save(User.builder()
                .username("sv_other")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("Sinh viên ngoài nhóm")
                .email("other@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleStudent))
                .isActive(true)
                .build());

        RegistrationPeriod period = periodRepository.save(RegistrationPeriod.builder()
                .name("Đợt test nộp báo cáo")
                .periodType(PeriodType.CAPSTONE)
                .status(PeriodStatus.OPEN_STUDENT)
                .lecturerStartDate(LocalDateTime.now().minusDays(5))
                .lecturerEndDate(LocalDateTime.now().minusDays(3))
                .studentStartDate(LocalDateTime.now().minusDays(2))
                .studentEndDate(LocalDateTime.now().plusDays(10))
                .defenseDate(LocalDateTime.now().plusDays(20))
                .build());

        Topic topic = topicRepository.save(Topic.builder()
                .code("TOPIC-SUBMIT-01")
                .title("Đề tài nghiên cứu")
                .department(dept)
                .period(period)
                .status(TopicStatus.APPROVED)
                .build());

        // Nhóm có đề tài
        groupWithTopic = studentGroupService.createGroup(
                CreateGroupDTO.builder().groupCode("GRP-HAS-TOPIC").periodId(period.getId()).build(),
                studentLeader
        );
        studentGroupService.addMember(groupWithTopic.getId(), "sv_member", studentLeader);
        studentGroupService.registerTopic(groupWithTopic.getId(), topic.getId(), studentLeader);

        // Nhóm không có đề tài
        groupWithoutTopic = studentGroupService.createGroup(
                CreateGroupDTO.builder().groupCode("GRP-NO-TOPIC").periodId(period.getId()).build(),
                otherStudent
        );
    }

    @Test
    @DisplayName("Nộp báo cáo thành công khi người nộp là Nhóm trưởng")
    void testSubmitReport_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "reportFile", "bao_cao_tuan_1.pdf", "application/pdf", "Dummy PDF content".getBytes()
        );

        SubmissionDTO dto = SubmissionDTO.builder()
                .groupId(groupWithTopic.getId())
                .title("Báo cáo tiến độ tuần 1")
                .notes("Đã hoàn thành phân tích yêu cầu")
                .build();

        Submission sub = submissionService.submitReport(dto, file, studentLeader);

        assertNotNull(sub.getId());
        assertEquals("Báo cáo tiến độ tuần 1", sub.getTitle());
        assertEquals(studentLeader.getId(), sub.getSubmittedBy().getId());
        assertNotNull(sub.getFilePath());
    }

    @Test
    @DisplayName("Chặn thành viên thường nộp báo cáo (Chỉ Nhóm trưởng mới được nộp)")
    void testSubmitReport_FailsWhenMemberSubmits() {
        MockMultipartFile file = new MockMultipartFile(
                "reportFile", "bao_cao.pdf", "application/pdf", "Dummy".getBytes()
        );

        SubmissionDTO dto = SubmissionDTO.builder()
                .groupId(groupWithTopic.getId())
                .title("Báo cáo trái quyền")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            submissionService.submitReport(dto, file, studentMember);
        });

        assertTrue(ex.getMessage().contains("chỉ duy nhất Nhóm trưởng"));
    }

    @Test
    @DisplayName("Chặn nộp báo cáo khi nhóm chưa đăng ký đề tài")
    void testSubmitReport_FailsWhenNoTopic() {
        MockMultipartFile file = new MockMultipartFile(
                "reportFile", "bao_cao.pdf", "application/pdf", "Dummy".getBytes()
        );

        SubmissionDTO dto = SubmissionDTO.builder()
                .groupId(groupWithoutTopic.getId())
                .title("Báo cáo khi chưa có đề tài")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            submissionService.submitReport(dto, file, otherStudent);
        });

        assertTrue(ex.getMessage().contains("chưa đăng ký đề tài"));
    }
}
