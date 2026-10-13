package vn.edu.ute.capstone.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.ute.capstone.entity.Topic;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.repository.StudentGroupRepository;
import vn.edu.ute.capstone.repository.TopicRepository;
import vn.edu.ute.capstone.service.EvaluationService;
import vn.edu.ute.capstone.service.ExcelService;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PublicResultController {

    private final TopicRepository topicRepository;
    private final StudentGroupRepository groupRepository;
    private final EvaluationService evaluationService;
    private final RegistrationPeriodRepository periodRepository;
    private final ExcelService excelService;

    @GetMapping("/admin/publish/export-excel")
    public ResponseEntity<InputStreamResource> exportResultsToExcel(
            @RequestParam(name = "periodId", required = false) Long periodId
    ) {
        ByteArrayInputStream in = excelService.exportResultsToExcel(periodId);
        String filename = "Bang_Diem_Tong_Ket_" + (periodId != null ? "Dot_" + periodId : "Tat_Ca") + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    private final vn.edu.ute.capstone.service.StudentGroupService studentGroupService;
    private final vn.edu.ute.capstone.service.UserService userService;

    @GetMapping({"/admin/publish", "/student/results"})
    public String viewResults(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            jakarta.servlet.http.HttpServletRequest request,
            Model model
    ) {
        String currentUrl = request.getRequestURI();
        boolean isStudent = request.isUserInRole("ROLE_STUDENT");

        List<Topic> topics;
        if (isStudent && userDetails != null) {
            vn.edu.ute.capstone.entity.User student = userService.findByUsername(userDetails.getUsername());
            List<vn.edu.ute.capstone.entity.StudentGroup> myGroups = studentGroupService.getGroupsByStudent(student.getId());
            topics = myGroups.stream()
                    .map(vn.edu.ute.capstone.entity.StudentGroup::getTopic)
                    .filter(java.util.Objects::nonNull)
                    .filter(t -> (periodId == null || periodId <= 0 || t.getPeriod().getId().equals(periodId)))
                    .distinct()
                    .toList();
        } else {
            topics = (periodId != null && periodId > 0)
                    ? topicRepository.findByPeriodId(periodId)
                    : topicRepository.findAll();
        }

        Map<Long, Double> averageScores = new HashMap<>();
        Map<Long, List<vn.edu.ute.capstone.entity.Evaluation>> topicEvaluations = new HashMap<>();

        for (Topic t : topics) {
            Double avg = evaluationService.getAverageScore(t.getId());
            averageScores.put(t.getId(), avg);
            topicEvaluations.put(t.getId(), evaluationService.getEvaluationsByTopic(t.getId(), null));
        }

        model.addAttribute("topics", topics);
        model.addAttribute("averageScores", averageScores);
        model.addAttribute("topicEvaluations", topicEvaluations);
        model.addAttribute("selectedPeriodId", periodId);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("currentUrl", currentUrl);
        model.addAttribute("isStudent", isStudent);

        return "results/list";
    }
}
