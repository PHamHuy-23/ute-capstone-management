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
import vn.edu.ute.capstone.dto.TopicProposalDTO;
import vn.edu.ute.capstone.entity.PeriodStatus;
import vn.edu.ute.capstone.entity.Topic;
import vn.edu.ute.capstone.entity.User;
import vn.edu.ute.capstone.repository.DepartmentRepository;
import vn.edu.ute.capstone.repository.RegistrationPeriodRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.edu.ute.capstone.service.TopicService;
import vn.edu.ute.capstone.service.UserService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/lecturer/topics")
@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
@RequiredArgsConstructor
public class LecturerTopicController {

    private final TopicService topicService;
    private final UserService userService;
    private final RegistrationPeriodRepository periodRepository;
    private final DepartmentRepository departmentRepository;
    private final vn.edu.ute.capstone.service.ExcelService excelService;

    @GetMapping
    public String myTopics(
            @RequestParam(name = "periodId", required = false) Long periodId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User currentLecturer = userService.findByUsername(userDetails.getUsername());
        org.springframework.data.domain.Page<Topic> topicPage = topicService.getTopicsForLecturerPaged(currentLecturer.getId(), periodId, page, size);

        model.addAttribute("topicPage", topicPage);
        model.addAttribute("topics", topicPage.getContent());
        model.addAttribute("selectedPeriodId", periodId);
        // Chỉ hiển thị các đợt đã công bố hoặc mở cho GV lọc (hoặc tất cả các đợt của chính đề tài mình)
        model.addAttribute("periods", periodRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("activePeriods", periodRepository.findByStatus(PeriodStatus.OPEN_LECTURER));
        return "lecturer/topics/list";
    }

    @GetMapping("/template")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.InputStreamResource> downloadTemplate() {
        java.io.ByteArrayInputStream in = excelService.generateTopicTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Mau_Dang_Ky_De_Tai_HCMUTE.xlsx");

        return org.springframework.http.ResponseEntity.ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new org.springframework.core.io.InputStreamResource(in));
    }

    @PostMapping("/upload-excel")
    public String uploadExcel(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("periodId") Long periodId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User currentLecturer = userService.findByUsername(userDetails.getUsername());

        try {
            Long deptId = (currentLecturer.getDepartment() != null) ? currentLecturer.getDepartment().getId() : 1L;
            List<TopicProposalDTO> dtos = excelService.parseTopicExcelFile(file, periodId, deptId);

            int count = 0;
            for (TopicProposalDTO dto : dtos) {
                topicService.createTopic(dto, currentLecturer);
                count++;
            }

            redirectAttributes.addFlashAttribute("successMessage", "Nhập thành công " + count + " đề tài từ file Excel!");
        } catch (Exception e) {
            log.error("Lỗi khi nhập đề tài từ Excel: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/lecturer/topics";
    }

    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentLecturer = userService.findByUsername(userDetails.getUsername());

        if (!model.containsAttribute("topicForm")) {
            TopicProposalDTO form = new TopicProposalDTO();
            if (currentLecturer.getDepartment() != null) {
                form.setDepartmentId(currentLecturer.getDepartment().getId());
            }
            model.addAttribute("topicForm", form);
        }

        populateFormReferenceData(model, currentLecturer);
        return "lecturer/topics/create";
    }

    @PostMapping("/create")
    public String createTopic(
            @Valid @ModelAttribute("topicForm") TopicProposalDTO topicForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User currentLecturer = userService.findByUsername(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            populateFormReferenceData(model, currentLecturer);
            return "lecturer/topics/create";
        }

        try {
            topicService.createTopic(topicForm, currentLecturer);
            redirectAttributes.addFlashAttribute("successMessage", "Đề xuất đề tài '" + topicForm.getTitle() + "' thành công!");
            return "redirect:/lecturer/topics";
        } catch (Exception e) {
            log.error("Lỗi khi tạo đề tài: ", e);
            model.addAttribute("errorMessage", e.getMessage());
            populateFormReferenceData(model, currentLecturer);
            return "lecturer/topics/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User currentLecturer = userService.findByUsername(userDetails.getUsername());

        try {
            Topic topic = topicService.findById(id);

            // Kiểm tra GV có phải là người hướng dẫn đề tài này không
            boolean isAdvisor = topic.getAdvisors().stream()
                    .anyMatch(a -> a.getLecturer().getId().equals(currentLecturer.getId()));
            if (!isAdvisor) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa đề tài này.");
                return "redirect:/lecturer/topics";
            }

            if (!model.containsAttribute("topicForm")) {
                Long coLecturerId = topic.getAdvisors().stream()
                        .filter(a -> !Boolean.TRUE.equals(a.getIsPrimary()))
                        .map(a -> a.getLecturer().getId())
                        .findFirst()
                        .orElse(null);

                TopicProposalDTO form = TopicProposalDTO.builder()
                        .id(topic.getId())
                        .code(topic.getCode())
                        .title(topic.getTitle())
                        .description(topic.getDescription())
                        .requirements(topic.getRequirements())
                        .periodId(topic.getPeriod().getId())
                        .departmentId(topic.getDepartment().getId())
                        .coLecturerId(coLecturerId)
                        .build();
                model.addAttribute("topicForm", form);
            }

            model.addAttribute("topic", topic);
            populateFormReferenceData(model, currentLecturer);
            return "lecturer/topics/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/lecturer/topics";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateTopic(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("topicForm") TopicProposalDTO topicForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User currentLecturer = userService.findByUsername(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            Topic topic = topicService.findById(id);
            model.addAttribute("topic", topic);
            populateFormReferenceData(model, currentLecturer);
            return "lecturer/topics/edit";
        }

        try {
            topicService.updateTopic(id, topicForm, currentLecturer);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đề tài thành công!");
            return "redirect:/lecturer/topics";
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật đề tài: ", e);
            Topic topic = topicService.findById(id);
            model.addAttribute("topic", topic);
            model.addAttribute("errorMessage", e.getMessage());
            populateFormReferenceData(model, currentLecturer);
            return "lecturer/topics/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTopic(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User currentLecturer = userService.findByUsername(userDetails.getUsername());
        try {
            topicService.deleteTopic(id, currentLecturer);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đề tài thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/lecturer/topics";
    }

    private void populateFormReferenceData(Model model, User currentLecturer) {
        // Lấy các đợt đang mở cho GV đề xuất
        model.addAttribute("activePeriods", periodRepository.findByStatus(PeriodStatus.OPEN_LECTURER));
        // Lấy danh sách bộ môn
        model.addAttribute("departments", departmentRepository.findAll());
        // Lấy danh sách các giảng viên khác (để chọn làm GV đồng hướng dẫn)
        List<User> otherLecturers = userService.getAllUsers().stream()
                .filter(u -> !u.getId().equals(currentLecturer.getId()))
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_LECTURER".equals(r.getName())))
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .toList();
        model.addAttribute("otherLecturers", otherLecturers);
    }
}
