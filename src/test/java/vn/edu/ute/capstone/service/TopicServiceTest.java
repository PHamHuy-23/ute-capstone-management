package vn.edu.ute.capstone.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.TopicApprovalDTO;
import vn.edu.ute.capstone.dto.TopicProposalDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.DepartmentRepository;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TopicServiceTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private RegistrationPeriodRepository periodRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    private User lecturer1;
    private User lecturer2;
    private Department dept;
    private RegistrationPeriod openPeriod;
    private RegistrationPeriod closedPeriod;

    @Autowired
    private vn.edu.ute.capstone.repository.RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        Role roleLecturer = roleRepository.save(Role.builder()
                .name("ROLE_LECTURER")
                .build());

        Role roleAdmin = roleRepository.save(Role.builder()
                .name("ROLE_ADMIN")
                .build());

        dept = departmentRepository.save(Department.builder()
                .name("Công nghệ phần mềm")
                .code("SE")
                .build());

        lecturer1 = userRepository.save(User.builder()
                .username("gv001")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("TS. Nguyễn Văn A")
                .email("nva@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleLecturer, roleAdmin))
                .isActive(true)
                .build());

        lecturer2 = userRepository.save(User.builder()
                .username("gv002")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("ThS. Trần Thị B")
                .email("ttb@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleLecturer))
                .isActive(true)
                .build());

        // Tạo đợt mở cho GV
        openPeriod = periodRepository.save(RegistrationPeriod.builder()
                .name("Đợt test đề xuất đề tài")
                .periodType(PeriodType.CAPSTONE)
                .status(PeriodStatus.OPEN_LECTURER)
                .lecturerStartDate(LocalDateTime.now().minusDays(1))
                .lecturerEndDate(LocalDateTime.now().plusDays(10))
                .studentStartDate(LocalDateTime.now().plusDays(11))
                .studentEndDate(LocalDateTime.now().plusDays(20))
                .defenseDate(LocalDateTime.now().plusDays(30))
                .build());

        // Tạo đợt chưa mở (DRAFT)
        closedPeriod = periodRepository.save(RegistrationPeriod.builder()
                .name("Đợt nháp chưa mở")
                .periodType(PeriodType.CAPSTONE)
                .status(PeriodStatus.DRAFT)
                .lecturerStartDate(LocalDateTime.now().plusDays(5))
                .lecturerEndDate(LocalDateTime.now().plusDays(15))
                .studentStartDate(LocalDateTime.now().plusDays(16))
                .studentEndDate(LocalDateTime.now().plusDays(25))
                .defenseDate(LocalDateTime.now().plusDays(35))
                .build());
    }

    @Test
    @DisplayName("Đề xuất đề tài thành công khi hợp lệ cả về trạng thái đợt và người hướng dẫn")
    void testCreateTopic_Success() {
        Long coLecturerId = (lecturer2 != null) ? lecturer2.getId() : null;

        TopicProposalDTO dto = TopicProposalDTO.builder()
                .code("TEST-DT01")
                .title("Ứng dụng AI trong quản lý đề tài tốt nghiệp")
                .description("Phát triển nền tảng gợi ý đề tài thông minh")
                .requirements("Java, Spring Boot, Python")
                .periodId(openPeriod.getId())
                .departmentId(dept.getId())
                .coLecturerId(coLecturerId)
                .build();

        Topic created = topicService.createTopic(dto, lecturer1);

        assertNotNull(created.getId());
        assertEquals("TEST-DT01", created.getCode());
        assertEquals(TopicStatus.PENDING_DEPT, created.getStatus());
        assertTrue(created.getAdvisors().stream().anyMatch(a -> a.getLecturer().getId().equals(lecturer1.getId()) && a.getIsPrimary()));
        if (coLecturerId != null) {
            assertEquals(2, created.getAdvisors().size());
            assertTrue(created.getAdvisors().stream().anyMatch(a -> a.getLecturer().getId().equals(coLecturerId) && !a.getIsPrimary()));
        }
    }

    @Test
    @DisplayName("Chặn đề xuất đề tài khi đợt chưa mở cho giảng viên (DRAFT)")
    void testCreateTopic_FailsWhenPeriodNotOpen() {
        TopicProposalDTO dto = TopicProposalDTO.builder()
                .code("TEST-DT02")
                .title("Đề tài không hợp lệ do đợt chưa mở")
                .periodId(closedPeriod.getId())
                .departmentId(dept.getId())
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            topicService.createTopic(dto, lecturer1);
        });

        assertTrue(ex.getMessage().contains("hiện không mở cho giảng viên"));
    }

    @Test
    @DisplayName("Chặn đề xuất khi giảng viên tự chọn chính mình làm đồng hướng dẫn")
    void testCreateTopic_FailsWhenSelfCoLecturer() {
        TopicProposalDTO dto = TopicProposalDTO.builder()
                .code("TEST-DT03")
                .title("Đề tài trùng GVHD")
                .periodId(openPeriod.getId())
                .departmentId(dept.getId())
                .coLecturerId(lecturer1.getId())
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            topicService.createTopic(dto, lecturer1);
        });

        assertTrue(ex.getMessage().contains("không thể chọn chính mình"));
    }

    @Test
    @DisplayName("Xét duyệt: Duyệt thành công và Từ chối bắt buộc có lý do")
    void testApproveOrRejectTopic() {
        TopicProposalDTO dto = TopicProposalDTO.builder()
                .code("TEST-DT04")
                .title("Đề tài kiểm tra phê duyệt")
                .periodId(openPeriod.getId())
                .departmentId(dept.getId())
                .build();

        Topic topic = topicService.createTopic(dto, lecturer1);

        // Trường hợp 1: Từ chối nhưng không nhập lý do -> Phải ném ngoại lệ
        TopicApprovalDTO rejectWithoutReason = TopicApprovalDTO.builder()
                .topicId(topic.getId())
                .approved(false)
                .rejectionReason("")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            topicService.approveOrRejectTopic(rejectWithoutReason, lecturer1);
        });

        // Trường hợp 2: Duyệt thành công
        TopicApprovalDTO approveDTO = TopicApprovalDTO.builder()
                .topicId(topic.getId())
                .approved(true)
                .build();

        Topic approvedTopic = topicService.approveOrRejectTopic(approveDTO, lecturer1);
        assertEquals(TopicStatus.APPROVED, approvedTopic.getStatus());
        assertNull(approvedTopic.getRejection_reason());
    }
}
