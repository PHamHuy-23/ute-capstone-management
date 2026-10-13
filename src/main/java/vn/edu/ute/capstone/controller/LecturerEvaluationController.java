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

    private final vn.edu.ute.capstone.repository.StudentGroupRepository groupRepository;
    private final vn.edu.ute.capstone.repository.SubmissionRepository submissionRepository;

    @GetMapping
    public String myEvaluationDashboard(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @RequestParam(name = "topicId", required = false) Long targetTopicId,
            @RequestParam(name = "councilId", required = false) Long targetCouncilId,
            @RequestParam(name = "type", required = false) EvaluationType targetType,
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

        // 6. Bản đồ điểm đã chấm: key = "TOPIC_ID" + "_" + "TYPE", value = Evaluation
        java.util.Map<String, Evaluation> evalMap = new java.util.HashMap<>();
        for (Evaluation e : myEvaluations) {
            evalMap.put(e.getTopic().getId() + "_" + e.getEvaluationType().name(), e);
        }

        // 7. Bản đồ báo cáo đồ án nộp mới nhất của từng đề tài: key = TOPIC_ID, value = Submission
        java.util.Map<Long, Submission> latestSubmissionMap = new java.util.HashMap<>();
        for (StudentGroup g : groupRepository.findAll()) {
            if (g.getTopic() != null) {
                List<Submission> subs = submissionRepository.findByGroupIdOrderBySubmittedAtDesc(g.getId());
                if (!subs.isEmpty()) {
                    latestSubmissionMap.put(g.getTopic().getId(), subs.get(0));
                }
            }
        }

        // 8. Bản đồ vai trò trong hội đồng: key = COUNCIL_ID, value = CouncilRole
        java.util.Map<Long, CouncilRole> councilRoleMap = new java.util.HashMap<>();
        for (Council c : myCouncils) {
            c.getMembers().stream()
                    .filter(m -> m.getLecturer().getId().equals(lecturer.getId()))
                    .findFirst()
                    .ifPresent(m -> councilRoleMap.put(c.getId(), m.getRoleInCouncil()));
        }

        // Tạo form mặc định nếu có target params truyền vào từ nút "Chấm Điểm Ngay"
        EvaluationDTO form = new EvaluationDTO();
        if (targetTopicId != null) form.setTopicId(targetTopicId);
        if (targetCouncilId != null) form.setCouncilId(targetCouncilId);
        if (targetType != null) form.setEvaluationType(targetType);

        model.addAttribute("councils", myCouncils);
        model.addAttribute("councilRoleMap", councilRoleMap);
        model.addAttribute("guidedTopics", guidedTopics);
        model.addAttribute("councilTopics", councilTopics);
        model.addAttribute("reviewerTopics", reviewerTopics);
        model.addAttribute("myEvaluations", myEvaluations);
        model.addAttribute("evalMap", evalMap);
        model.addAttribute("latestSubmissionMap", latestSubmissionMap);
        model.addAttribute("selectedPeriodId", periodId);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("evaluationForm", form);
        model.addAttribute("evaluationTypes", EvaluationType.values());
        model.addAttribute("targetTopicId", targetTopicId);
        model.addAttribute("targetCouncilId", targetCouncilId);
        model.addAttribute("targetType", targetType != null ? targetType.name() : "");

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
