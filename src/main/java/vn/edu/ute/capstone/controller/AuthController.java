package vn.edu.ute.capstone.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.ute.capstone.dto.UserProfileDTO;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.repository.StudentGroupRepository;
import vn.edu.ute.capstone.repository.TopicRepository;
import vn.edu.ute.capstone.repository.UserRepository;
import vn.edu.ute.capstone.service.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TopicRepository topicRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final UserRepository userRepository;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "oauth2_error", required = false) String oauth2Error,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tài khoản hoặc mật khẩu không chính xác!");
        }
        if (oauth2Error != null) {
            model.addAttribute("errorMessage", "Đăng nhập Google không thành công. Lưu ý: Chỉ chấp nhận email chính thức do HCMUTE cấp (@student.hcmute.edu.vn hoặc @ute.edu.vn) và đã có trong hệ thống.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Đăng xuất thành công!");
        }
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserProfileDTO profile = userService.getProfileByUsername(username);

            model.addAttribute("profile", profile);
            model.addAttribute("fullName", profile.getFullName());
            model.addAttribute("departmentName", profile.getDepartmentName());

            // Thống kê nhanh toàn hệ thống
            model.addAttribute("totalTopics", topicRepository.count());
            model.addAttribute("totalGroups", studentGroupRepository.count());
            model.addAttribute("totalPeriods", registrationPeriodRepository.count());
            model.addAttribute("totalUsers", userRepository.count());

            // Dữ liệu biểu đồ trực quan (Neo-Brutalism Charts)
            java.util.List<Object[]> deptStats = topicRepository.countTopicsGroupByDepartment();
            java.util.List<String> deptLabels = new java.util.ArrayList<>();
            java.util.List<Long> deptCounts = new java.util.ArrayList<>();
            for (Object[] row : deptStats) {
                deptLabels.add(row[0] != null ? row[0].toString() : "Khác");
                deptCounts.add(((Number) row[1]).longValue());
            }
            model.addAttribute("deptChartLabels", deptLabels);
            model.addAttribute("deptChartCounts", deptCounts);

            java.util.List<Object[]> statusStats = topicRepository.countTopicsGroupByStatus();
            java.util.List<String> statusLabels = new java.util.ArrayList<>();
            java.util.List<Long> statusCounts = new java.util.ArrayList<>();
            for (Object[] row : statusStats) {
                vn.edu.ute.capstone.entity.TopicStatus st = (vn.edu.ute.capstone.entity.TopicStatus) row[0];
                statusLabels.add(st != null ? st.getDescription() : "Khác");
                statusCounts.add(((Number) row[1]).longValue());
            }
            model.addAttribute("statusChartLabels", statusLabels);
            model.addAttribute("statusChartCounts", statusCounts);
        }
        return "dashboard";
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Authentication authentication, Model model) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("roles", authentication.getAuthorities());
        }
        return "auth/access-denied";
    }

}
