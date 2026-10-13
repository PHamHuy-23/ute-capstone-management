package vn.edu.ute.capstone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.ute.capstone.dto.TopicApprovalDTO;
import vn.edu.ute.capstone.entity.Topic;
import vn.edu.ute.capstone.entity.TopicStatus;
import vn.edu.ute.capstone.entity.User;
import vn.edu.ute.capstone.repository.DepartmentRepository;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.edu.ute.capstone.service.TopicService;
import vn.edu.ute.capstone.service.UserService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/department")
@PreAuthorize("hasAnyRole('DEPARTMENT_HEAD', 'ADMIN')")
@RequiredArgsConstructor
public class DepartmentTopicController {

    private final TopicService topicService;
    private final UserService userService;
    private final RegistrationPeriodRepository periodRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/topics")
    public String listTopicsForDepartment(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));

        List<Topic> topics;
        if (isAdmin) {
            topics = topicService.getAllTopics(periodId);
        } else {
            if (currentUser.getDepartment() == null) {
                model.addAttribute("errorMessage", "Tài khoản của bạn chưa được liên kết với Bộ môn nào.");
                topics = List.of();
            } else {
                topics = topicService.getTopicsForDepartment(currentUser.getDepartment().getId(), periodId);
            }
        }

        model.addAttribute("topics", topics);
        model.addAttribute("selectedPeriodId", periodId);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("approvalForm", new TopicApprovalDTO());
        return "department/topics/list";
    }

    @PostMapping("/topics/approve")
    public String approveTopic(
            @Valid @ModelAttribute("approvalForm") TopicApprovalDTO approvalForm,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            Topic topic = topicService.approveOrRejectTopic(approvalForm, currentUser);
            if (Boolean.TRUE.equals(approvalForm.getApproved())) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt đề tài '" + topic.getTitle() + "' thành công!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đề tài '" + topic.getTitle() + "'. Đã gửi phản hồi lý do cho GV.");
            }
        } catch (Exception e) {
            log.error("Lỗi khi xét duyệt đề tài: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/department/topics";
    }

    @GetMapping("/catalog")
    public String viewCatalog(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));

        List<Topic> topics;
        if (isAdmin) {
            topics = topicService.getAllTopics(periodId);
        } else {
            if (currentUser.getDepartment() == null) {
                model.addAttribute("errorMessage", "Tài khoản của bạn chưa được liên kết với Bộ môn nào.");
                topics = List.of();
            } else {
                topics = topicService.getTopicsForDepartment(currentUser.getDepartment().getId(), periodId);
            }
        }

        model.addAttribute("topics", topics);
        model.addAttribute("selectedPeriodId", periodId);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", isAdmin);

        return "department/topics/catalog";
    }
}

