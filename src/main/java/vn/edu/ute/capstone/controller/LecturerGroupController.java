package vn.edu.ute.capstone.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.edu.ute.capstone.repository.StudentGroupRepository;
import vn.edu.ute.capstone.service.SubmissionService;
import vn.edu.ute.capstone.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/lecturer/groups")
@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
@RequiredArgsConstructor
public class LecturerGroupController {

    private final StudentGroupRepository groupRepository;
    private final SubmissionService submissionService;
    private final UserService userService;
    private final RegistrationPeriodRepository periodRepository;

    @GetMapping
    public String listMyGroups(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User lecturer = userService.findByUsername(userDetails.getUsername());

        // Lấy tất cả nhóm sinh viên đã nhận đề tài mà giảng viên này đang hướng dẫn
        List<StudentGroup> allGroups = (periodId != null && periodId > 0) 
                ? groupRepository.findByPeriodId(periodId) 
                : groupRepository.findAll();

        List<StudentGroup> myGuidedGroups = allGroups.stream()
                .filter(g -> g.getTopic() != null)
                .filter(g -> g.getTopic().getAdvisors().stream()
                        .anyMatch(a -> a.getLecturer().getId().equals(lecturer.getId())))
                .toList();

        model.addAttribute("groups", myGuidedGroups);
        model.addAttribute("selectedPeriodId", periodId);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());

        return "lecturer/groups/list";
    }

    @GetMapping("/{id}/submissions")
    public String viewGroupSubmissions(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User lecturer = userService.findByUsername(userDetails.getUsername());
        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm sinh viên với ID: " + id));

        List<Submission> submissions = submissionService.getSubmissionsByGroup(id, lecturer);

        model.addAttribute("group", group);
        model.addAttribute("submissions", submissions);

        return "lecturer/groups/submissions";
    }

    @GetMapping("/download-submission/{id}")
    public ResponseEntity<Resource> downloadSubmission(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User lecturer = userService.findByUsername(userDetails.getUsername());
        Submission submission = submissionService.findById(id);
        Resource resource = submissionService.loadReportFile(id, lecturer);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + submission.getFilePath() + "\"")
                .body(resource);
    }
}
