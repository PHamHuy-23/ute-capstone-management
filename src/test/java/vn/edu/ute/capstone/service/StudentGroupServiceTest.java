package vn.edu.ute.capstone.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.CreateGroupDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StudentGroupServiceTest {

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

    private User student1;
    private User student2;
    private User student3;
    private User student4;
    private RegistrationPeriod openPeriod;
    private Topic approvedTopic;
    private Topic anotherTopic;

    @BeforeEach
    void setUp() {
        Role roleStudent = roleRepository.save(Role.builder()
                .name("ROLE_STUDENT")
                .build());

        Department dept = departmentRepository.save(Department.builder()
                .name("Công nghệ phần mềm")
                .code("SE")
                .build());

        student1 = userRepository.save(User.builder()
                .username("sv001")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("Sinh viên 1 (Leader)")
                .email("sv001@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleStudent))
                .isActive(true)
                .build());

        student2 = userRepository.save(User.builder()
                .username("sv002")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("Sinh viên 2")
                .email("sv002@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleStudent))
                .isActive(true)
                .build());

        student3 = userRepository.save(User.builder()
                .username("sv003")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("Sinh viên 3")
                .email("sv003@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleStudent))
                .isActive(true)
                .build());

        student4 = userRepository.save(User.builder()
                .username("sv004")
                .password("$2a$10$w8uD.30Q4u03X4Q9QY/5yeV89i75o01P1X3X2b/f4M6t5Y/3E7eUu")
                .fullName("Sinh viên 4")
                .email("sv004@ute.edu.vn")
                .department(dept)
                .roles(java.util.Set.of(roleStudent))
                .isActive(true)
                .build());

        openPeriod = periodRepository.save(RegistrationPeriod.builder()
                .name("Đợt KLTN Học kỳ 1 2026")
                .periodType(PeriodType.CAPSTONE)
                .status(PeriodStatus.OPEN_STUDENT)
                .lecturerStartDate(LocalDateTime.now().minusDays(10))
                .lecturerEndDate(LocalDateTime.now().minusDays(5))
                .studentStartDate(LocalDateTime.now().minusDays(2))
                .studentEndDate(LocalDateTime.now().plusDays(10))
                .defenseDate(LocalDateTime.now().plusDays(20))
                .build());

        approvedTopic = topicRepository.save(Topic.builder()
                .code("TOPIC-TEST-01")
                .title("Xây dựng sàn thương mại điện tử")
                .department(dept)
                .period(openPeriod)
                .status(TopicStatus.APPROVED)
                .build());

        anotherTopic = topicRepository.save(Topic.builder()
                .code("TOPIC-TEST-02")
                .title("Phát triển ứng dụng Mobile")
                .department(dept)
                .period(openPeriod)
                .status(TopicStatus.APPROVED)
                .build());
    }

    @Test
    @DisplayName("Tạo nhóm thành công: Người tạo tự động làm Leader")
    void testCreateGroup_Success() {
        CreateGroupDTO dto = CreateGroupDTO.builder()
                .groupCode("GRP-TEST-01")
                .periodId(openPeriod.getId())
                .build();

        StudentGroup group = studentGroupService.createGroup(dto, student1);

        assertNotNull(group.getId());
        assertEquals("GRP-TEST-01", group.getGroupCode());
        assertEquals(1, group.getMembers().size());
        assertTrue(group.getMembers().get(0).getIsLeader());
        assertEquals(student1.getId(), group.getMembers().get(0).getStudent().getId());
    }

    @Test
    @DisplayName("Chặn 1 Sinh viên tham gia 2 nhóm trong cùng 1 đợt")
    void testCreateGroup_FailsWhenStudentAlreadyInAnotherGroup() {
        CreateGroupDTO dto1 = CreateGroupDTO.builder()
                .groupCode("GRP-TEST-01")
                .periodId(openPeriod.getId())
                .build();
        studentGroupService.createGroup(dto1, student1);

        CreateGroupDTO dto2 = CreateGroupDTO.builder()
                .groupCode("GRP-TEST-02")
                .periodId(openPeriod.getId())
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            studentGroupService.createGroup(dto2, student1);
        });

        assertTrue(ex.getMessage().contains("Mỗi sinh viên chỉ được thuộc duy nhất 1 nhóm"));
    }

    @Test
    @DisplayName("Thêm thành viên: Chặn khi vượt quá tối đa 3 sinh viên")
    void testAddMember_FailsWhenExceedsLimit() {
        CreateGroupDTO dto = CreateGroupDTO.builder()
                .groupCode("GRP-TEST-01")
                .periodId(openPeriod.getId())
                .build();
        StudentGroup group = studentGroupService.createGroup(dto, student1);

        // Thêm SV thứ 2
        studentGroupService.addMember(group.getId(), "sv002", student1);
        // Thêm SV thứ 3
        studentGroupService.addMember(group.getId(), "sv003", student1);

        // Thêm SV thứ 4 -> Phải ném ngoại lệ
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            studentGroupService.addMember(group.getId(), "sv004", student1);
        });

        assertTrue(ex.getMessage().contains("tối đa 3 thành viên"));
    }

    @Test
    @DisplayName("Đăng ký đề tài độc quyền: Chặn nhóm thứ 2 đăng ký cùng đề tài")
    void testRegisterTopic_ExclusiveRegistration() {
        // Tạo nhóm 1
        StudentGroup group1 = studentGroupService.createGroup(
                CreateGroupDTO.builder().groupCode("GRP-01").periodId(openPeriod.getId()).build(), 
                student1
        );

        // Tạo nhóm 2
        StudentGroup group2 = studentGroupService.createGroup(
                CreateGroupDTO.builder().groupCode("GRP-02").periodId(openPeriod.getId()).build(), 
                student2
        );

        // Nhóm 1 đăng ký đề tài approvedTopic thành công
        StudentGroup updatedGroup1 = studentGroupService.registerTopic(group1.getId(), approvedTopic.getId(), student1);
        assertNotNull(updatedGroup1.getTopic());
        assertEquals(TopicStatus.ASSIGNED, updatedGroup1.getTopic().getStatus());

        // Nhóm 2 cố tình đăng ký cùng đề tài approvedTopic -> Phải bị chặn
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            studentGroupService.registerTopic(group2.getId(), approvedTopic.getId(), student2);
        });

        assertTrue(ex.getMessage().contains("chưa sẵn sàng để đăng ký") || ex.getMessage().contains("nhóm khác đăng ký"));
    }
}
