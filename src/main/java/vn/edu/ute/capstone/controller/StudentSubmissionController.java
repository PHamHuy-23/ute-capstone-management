package vn.edu.ute.capstone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.ute.capstone.dto.SubmissionDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import vn.edu.ute.capstone.service.StudentGroupService;
import vn.edu.ute.capstone.service.SubmissionService;
import vn.edu.ute.capstone.service.UserService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/student/submissions")
@RequiredArgsConstructor
public class StudentSubmissionController {

    private final SubmissionService submissionService;
    private final StudentGroupService groupService;
    private final UserService userService;
    private final RegistrationPeriodRepository periodRepository;

    @GetMapping
    public String viewSubmissions(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User student = userService.findByUsername(userDetails.getUsername());
        List<RegistrationPeriod> periods = periodRepository.findAllByOrderByCreatedAtDesc();

        Long currentPeriodId = periodId;
        if (currentPeriodId == null) {
            // Ưu tiên 1: Lấy đợt mà sinh viên ĐÃ CÓ NHÓM (để không bị mất nhóm khi đợt trước đó đóng)
            List<StudentGroup> myGroups = groupService.getGroupsByStudent(student.getId());
            if (!myGroups.isEmpty()) {
                currentPeriodId = myGroups.get(0).getPeriod().getId();
            } else if (!periods.isEmpty()) {
                currentPeriodId = periods.get(0).getId();
            }
        }

        StudentGroup currentGroup = null;
        boolean isLeader = false;
        List<Submission> submissions = List.of();
        RegistrationPeriod selectedPeriod = null;

        if (currentPeriodId != null) {
            final Long pid = currentPeriodId;
            selectedPeriod = periods.stream().filter(p -> p.getId().equals(pid)).findFirst().orElse(null);
            Optional<StudentGroup> groupOpt = groupService.getGroupByStudentAndPeriod(student.getId(), currentPeriodId);
            if (groupOpt.isPresent()) {
                currentGroup = groupOpt.get();
                isLeader = currentGroup.getMembers().stream()
                        .anyMatch(m -> m.getStudent().getId().equals(student.getId()) && Boolean.TRUE.equals(m.getIsLeader()));
                submissions = submissionService.getSubmissionsByGroup(currentGroup.getId(), student);
            }
        }

        model.addAttribute("student", student);
        model.addAttribute("group", currentGroup);
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("submissions", submissions);
        model.addAttribute("selectedPeriodId", currentPeriodId);
        model.addAttribute("selectedPeriod", selectedPeriod);
        model.addAttribute("periods", periods);
        model.addAttribute("submissionForm", new SubmissionDTO());

        return "student/submissions/list";
    }

    @PostMapping("/upload")
    public String uploadSubmission(
            @Valid @ModelAttribute("submissionForm") SubmissionDTO submissionForm,
            @RequestParam("reportFile") MultipartFile reportFile,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User student = userService.findByUsername(userDetails.getUsername());

        try {
            submissionService.submitReport(submissionForm, reportFile, student);
            redirectAttributes.addFlashAttribute("successMessage", "Nộp báo cáo thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi nộp báo cáo: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/submissions";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User student = userService.findByUsername(userDetails.getUsername());
        Submission submission = submissionService.findById(id);
        Resource resource = submissionService.loadReportFile(id, student);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + submission.getFilePath() + "\"")
                .body(resource);
    }
}
