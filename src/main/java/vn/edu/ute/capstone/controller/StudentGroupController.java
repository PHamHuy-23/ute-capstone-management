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
import vn.edu.ute.capstone.dto.CreateGroupDTO;
import vn.edu.ute.capstone.entity.*;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.edu.ute.capstone.service.StudentGroupService;
import vn.edu.ute.capstone.service.UserService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/student")
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
@RequiredArgsConstructor
public class StudentGroupController {

    private final StudentGroupService groupService;
    private final UserService userService;
    private final RegistrationPeriodRepository periodRepository;

    @GetMapping("/group")
    public String viewMyGroup(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User student = userService.findByUsername(userDetails.getUsername());
        List<RegistrationPeriod> periods = periodRepository.findAllByOrderByCreatedAtDesc();
        List<RegistrationPeriod> activePeriods = periodRepository.findByStatus(PeriodStatus.OPEN_STUDENT);

        Long currentPeriodId = periodId;
        if (currentPeriodId == null) {
            // Ưu tiên 1: Lấy đợt mà sinh viên ĐÃ CÓ NHÓM (để khi đợt đóng, nhóm không bị biến mất)
            List<StudentGroup> myGroups = groupService.getGroupsByStudent(student.getId());
            if (!myGroups.isEmpty()) {
                currentPeriodId = myGroups.get(0).getPeriod().getId();
            } else if (!activePeriods.isEmpty()) {
                currentPeriodId = activePeriods.get(0).getId();
            } else if (!periods.isEmpty()) {
                currentPeriodId = periods.get(0).getId();
            }
        }

        Optional<StudentGroup> groupOpt = Optional.empty();
        if (currentPeriodId != null) {
            groupOpt = groupService.getGroupByStudentAndPeriod(student.getId(), currentPeriodId);
        }

        boolean isLeader = false;
        if (groupOpt.isPresent()) {
            isLeader = groupOpt.get().getMembers().stream()
                    .anyMatch(m -> m.getStudent().getId().equals(student.getId()) && Boolean.TRUE.equals(m.getIsLeader()));
        }

        RegistrationPeriod selectedPeriod = null;
        if (currentPeriodId != null) {
            selectedPeriod = periodRepository.findById(currentPeriodId).orElse(null);
        }

        model.addAttribute("student", student);
        model.addAttribute("group", groupOpt.orElse(null));
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("selectedPeriodId", currentPeriodId);
        model.addAttribute("selectedPeriod", selectedPeriod);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("activePeriods", activePeriods);
        model.addAttribute("createGroupForm", new CreateGroupDTO());

        return "student/group/detail";
    }

    @PostMapping("/group/create")
    public String createGroup(
            @Valid @ModelAttribute("createGroupForm") CreateGroupDTO createGroupForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User student = userService.findByUsername(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ mã nhóm và chọn đợt hợp lệ.");
            return "redirect:/student/group?periodId=" + createGroupForm.getPeriodId();
        }

        try {
            groupService.createGroup(createGroupForm, student);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo nhóm thành công! Bạn hiện là Nhóm trưởng.");
        } catch (Exception e) {
            log.error("Lỗi khi tạo nhóm: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/group?periodId=" + createGroupForm.getPeriodId();
    }

    @PostMapping("/group/{groupId}/add-member")
    public String addMember(
            @PathVariable("groupId") Long groupId,
            @RequestParam("studentUsername") String studentUsername,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User student = userService.findByUsername(userDetails.getUsername());

        try {
            groupService.addMember(groupId, studentUsername, student);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm thành viên (" + studentUsername + ") vào nhóm thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi thêm thành viên: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/group";
    }

    @PostMapping("/group/{groupId}/remove-member/{memberId}")
    public String removeMember(
            @PathVariable("groupId") Long groupId,
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User student = userService.findByUsername(userDetails.getUsername());

        try {
            groupService.removeMember(groupId, memberId, student);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thành viên khỏi nhóm.");
        } catch (Exception e) {
            log.error("Lỗi khi xóa thành viên: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/group";
    }

    @PostMapping("/group/{groupId}/disband")
    public String disbandGroup(
            @PathVariable("groupId") Long groupId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User student = userService.findByUsername(userDetails.getUsername());

        try {
            groupService.disbandGroup(groupId, student);
            redirectAttributes.addFlashAttribute("successMessage", "Đã giải tán nhóm thành công.");
        } catch (Exception e) {
            log.error("Lỗi khi giải tán nhóm: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/group";
    }

    @GetMapping("/topics")
    public String browseTopics(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User student = userService.findByUsername(userDetails.getUsername());
        List<RegistrationPeriod> periods = periodRepository.findAllByOrderByCreatedAtDesc();
        List<RegistrationPeriod> activePeriods = periodRepository.findByStatus(PeriodStatus.OPEN_STUDENT);

        Long currentPeriodId = periodId;
        if (currentPeriodId == null) {
            List<StudentGroup> myGroups = groupService.getGroupsByStudent(student.getId());
            if (!myGroups.isEmpty()) {
                currentPeriodId = myGroups.get(0).getPeriod().getId();
            } else if (!activePeriods.isEmpty()) {
                currentPeriodId = activePeriods.get(0).getId();
            } else if (!periods.isEmpty()) {
                currentPeriodId = periods.get(0).getId();
            }
        }

        List<Topic> topics = groupService.getAvailableTopicsForStudent(currentPeriodId);

        StudentGroup studentGroup = null;
        boolean isLeader = false;
        if (currentPeriodId != null) {
            Optional<StudentGroup> groupOpt = groupService.getGroupByStudentAndPeriod(student.getId(), currentPeriodId);
            if (groupOpt.isPresent()) {
                studentGroup = groupOpt.get();
                isLeader = studentGroup.getMembers().stream()
                        .anyMatch(m -> m.getStudent().getId().equals(student.getId()) && Boolean.TRUE.equals(m.getIsLeader()));
            }
        }

        model.addAttribute("topics", topics);
        model.addAttribute("selectedPeriodId", currentPeriodId);
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("studentGroup", studentGroup);
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("student", student);

        return "student/topics/list";
    }

    @PostMapping("/topics/register")
    public String registerTopic(
            @RequestParam("groupId") Long groupId,
            @RequestParam("topicId") Long topicId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User student = userService.findByUsername(userDetails.getUsername());

        try {
            StudentGroup group = groupService.registerTopic(groupId, topicId, student);
            redirectAttributes.addFlashAttribute("successMessage", "Chúc mừng! Nhóm đã đăng ký thành công đề tài: " + group.getTopic().getTitle());
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký đề tài: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/topics";
    }

    @PostMapping("/topics/cancel")
    public String cancelTopic(
            @RequestParam("groupId") Long groupId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User student = userService.findByUsername(userDetails.getUsername());

        try {
            groupService.cancelTopicRegistration(groupId, student);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đăng ký đề tài thành công. Bạn có thể chọn đề tài khác.");
        } catch (Exception e) {
            log.error("Lỗi khi hủy đề tài: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/topics";
    }
}
