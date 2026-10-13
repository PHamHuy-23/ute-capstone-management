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
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tài khoản hoặc mật khẩu không chính xác!");
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
        }
        return "dashboard";
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/dashboard";
    }

}
