package vn.edu.ute.capstone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.ute.capstone.dto.CouncilCreateDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.repository.TopicRepository;
import vn.edu.ute.capstone.service.EvaluationService;
import vn.edu.ute.capstone.service.UserService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/councils")
@RequiredArgsConstructor
public class AdminCouncilController {

    private final EvaluationService evaluationService;
    private final UserService userService;
    private final RegistrationPeriodRepository periodRepository;
    private final TopicRepository topicRepository;

    @GetMapping
    public String listCouncils(
            @RequestParam(name = "periodId", required = false) Long periodId,
            Model model
    ) {
        List<Council> councils = evaluationService.getCouncilsByPeriod(periodId);

        model.addAttribute("councils", councils);
        model.addAttribute("selectedPeriodId", periodId);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());

        return "admin/councils/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("councilForm")) {
            model.addAttribute("councilForm", new CouncilCreateDTO());
        }

        populateFormData(model);
        return "admin/councils/create";
    }

    @PostMapping("/create")
    public String createCouncil(
            @Valid @ModelAttribute("councilForm") CouncilCreateDTO councilForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User admin = userService.findByUsername(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            populateFormData(model);
            return "admin/councils/create";
        }

        try {
            Council created = evaluationService.createCouncil(councilForm, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Thành lập hội đồng '" + created.getName() + "' thành công!");
            return "redirect:/admin/councils";
        } catch (Exception e) {
            log.error("Lỗi khi thành lập hội đồng: ", e);
            model.addAttribute("errorMessage", e.getMessage());
            populateFormData(model);
            return "admin/councils/create";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCouncil(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User admin = userService.findByUsername(userDetails.getUsername());
        try {
            evaluationService.deleteCouncil(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Đã giải tán/xóa hội đồng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/councils";
    }

    private void populateFormData(Model model) {
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        List<User> lecturers = userService.getAllUsers().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_LECTURER".equals(r.getName())))
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .toList();
        model.addAttribute("lecturers", lecturers);

        // Lấy danh sách đề tài đã duyệt (hoặc đã có nhóm) để phân công chấm
        List<Topic> approvedTopics = topicRepository.findAll().stream()
                .filter(t -> t.getStatus() == TopicStatus.APPROVED || t.getStatus() == TopicStatus.ASSIGNED)
                .toList();
        model.addAttribute("topics", approvedTopics);
    }
}
