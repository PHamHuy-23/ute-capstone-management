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
import vn.edu.ute.capstone.dto.EvaluationDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.repository.TopicRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.edu.ute.capstone.service.EvaluationService;
import vn.edu.ute.capstone.service.UserService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/lecturer/evaluations")
@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
@RequiredArgsConstructor
public class LecturerEvaluationController {

    private final EvaluationService evaluationService;
    private final TopicRepository topicRepository;
    private final UserService userService;
    private final RegistrationPeriodRepository periodRepository;

    @GetMapping
    public String myEvaluationDashboard(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User lecturer = userService.findByUsername(userDetails.getUsername());

        // 1. Lấy các hội đồng mà GV này tham gia
        List<Council> myCouncils = evaluationService.getCouncilsByLecturer(lecturer.getId());

        // 2. Đề tài mà GV này hướng dẫn (để chấm điểm hướng dẫn ADVISOR)
        List<Topic> guidedTopics = topicRepository.findAll().stream()
                .filter(t -> (periodId == null || t.getPeriod().getId().equals(periodId)))
                .filter(t -> t.getStatus() == TopicStatus.ASSIGNED || t.getStatus() == TopicStatus.APPROVED)
                .filter(t -> t.getAdvisors().stream().anyMatch(a -> a.getLecturer().getId().equals(lecturer.getId())))
                .toList();

        // 3. Đề tài thuộc các Hội đồng mà GV này ngồi ghế chấm (COUNCIL)
        List<Topic> councilTopics = myCouncils.stream()
                .flatMap(c -> c.getAssignedTopics().stream())
                .distinct()
                .filter(t -> (periodId == null || t.getPeriod().getId().equals(periodId)))
                .toList();

        // 4. Các đề tài khác có thể chấm phản biện (REVIEWER - Không phải GVHD)
        List<Topic> reviewerTopics = topicRepository.findAll().stream()
                .filter(t -> (periodId == null || t.getPeriod().getId().equals(periodId)))
                .filter(t -> t.getStatus() == TopicStatus.ASSIGNED || t.getStatus() == TopicStatus.APPROVED)
                .filter(t -> t.getAdvisors().stream().noneMatch(a -> a.getLecturer().getId().equals(lecturer.getId())))
                .toList();

        // 5. Lịch sử các điểm GV đã chấm
        List<Evaluation> myEvaluations = evaluationService.getEvaluationsByEvaluator(lecturer.getId());

        model.addAttribute("councils", myCouncils);
        model.addAttribute("guidedTopics", guidedTopics);
        model.addAttribute("councilTopics", councilTopics);
        model.addAttribute("reviewerTopics", reviewerTopics);
        model.addAttribute("myEvaluations", myEvaluations);
        model.addAttribute("selectedPeriodId", periodId);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("evaluationForm", new EvaluationDTO());
        model.addAttribute("evaluationTypes", EvaluationType.values());

        return "lecturer/evaluations/list";
    }

    @PostMapping("/submit")
    public String submitScore(
            @Valid @ModelAttribute("evaluationForm") EvaluationDTO evaluationForm,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User lecturer = userService.findByUsername(userDetails.getUsername());

        try {
            evaluationService.submitEvaluation(evaluationForm, lecturer);
            redirectAttributes.addFlashAttribute("successMessage", "Nhập điểm và nhận xét thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi nhập điểm: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/lecturer/evaluations";
    }
}
