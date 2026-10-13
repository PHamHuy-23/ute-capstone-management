package vn.edu.ute.capstone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.ute.capstone.dto.RegistrationPeriodDTO;
import vn.edu.ute.capstone.entity.PeriodStatus;
import vn.edu.ute.capstone.entity.PeriodType;
import vn.edu.ute.capstone.entity.RegistrationPeriod;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.edu.ute.capstone.service.ExcelService;
import vn.edu.ute.capstone.service.RegistrationPeriodService;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/admin/periods")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PeriodController {

    private final RegistrationPeriodService periodService;
    private final ExcelService excelService;
    private final vn.edu.ute.capstone.repository.DepartmentRepository departmentRepository;

    @GetMapping
    public String listPeriods(Model model) {
        model.addAttribute("periods", periodService.getAllPeriods());
        model.addAttribute("allStatuses", PeriodStatus.values());
        return "admin/periods/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("periodForm")) {
            model.addAttribute("periodForm", new RegistrationPeriodDTO());
        }
        model.addAttribute("periodTypes", PeriodType.values());
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/periods/create";
    }

    @PostMapping("/create")
    public String createPeriod(
            @Valid @ModelAttribute("periodForm") RegistrationPeriodDTO periodForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("periodTypes", PeriodType.values());
            model.addAttribute("departments", departmentRepository.findAll());
            return "admin/periods/create";
        }

        try {
            periodService.createPeriod(periodForm);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo đợt đăng ký mới thành công!");
            return "redirect:/admin/periods";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("periodTypes", PeriodType.values());
            model.addAttribute("departments", departmentRepository.findAll());
            return "admin/periods/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        RegistrationPeriod period = periodService.getPeriodById(id);
        RegistrationPeriodDTO dto = RegistrationPeriodDTO.builder()
                .id(period.getId())
                .name(period.getName())
                .periodType(period.getPeriodType())
                .lecturerStartDate(period.getLecturerStartDate())
                .lecturerEndDate(period.getLecturerEndDate())
                .studentStartDate(period.getStudentStartDate())
                .studentEndDate(period.getStudentEndDate())
                .reviewDeadline(period.getReviewDeadline())
                .defenseDate(period.getDefenseDate())
                .status(period.getStatus())
                .minCredits(period.getMinCredits())
                .minGpa(period.getMinGpa())
                .targetDepartmentId(period.getTargetDepartment() != null ? period.getTargetDepartment().getId() : null)
                .targetCohort(period.getTargetCohort())
                .requiresAdvisorApproval(period.getRequiresAdvisorApproval())
                .build();

        model.addAttribute("periodForm", dto);
        model.addAttribute("periodTypes", PeriodType.values());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("allStatuses", PeriodStatus.values());
        return "admin/periods/edit";
    }

    @PostMapping("/edit/{id}")
    public String updatePeriod(
            @PathVariable Long id,
            @Valid @ModelAttribute("periodForm") RegistrationPeriodDTO periodForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("periodTypes", PeriodType.values());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("allStatuses", PeriodStatus.values());
            return "admin/periods/edit";
        }

        try {
            periodService.updatePeriod(id, periodForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đợt đăng ký thành công!");
            return "redirect:/admin/periods";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("periodTypes", PeriodType.values());
            model.addAttribute("allStatuses", PeriodStatus.values());
            return "admin/periods/edit";
        }
    }

    @PostMapping("/status/{id}")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam("status") PeriodStatus status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            periodService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành: " + status.getDisplayName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/periods";
    }

    @PostMapping("/delete/{id}")
    public String deletePeriod(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            periodService.deletePeriod(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đợt đăng ký thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/periods";
    }

    // === TÍNH NĂNG EXCEL: TẢI TEMPLATE MẪU ===
    @GetMapping("/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        ByteArrayInputStream stream = excelService.generatePeriodTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Mau_Dot_Dang_Ky_HCMUTE.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    // === TÍNH NĂNG EXCEL: IMPORT BATCH NHIỀU ĐỢT KÈM TRANSACTION ===
    @PostMapping("/upload-excel")
    @Transactional
    public String uploadExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<RegistrationPeriodDTO> periodDTOs = excelService.parsePeriodExcelFile(file);

            int successCount = 0;
            for (RegistrationPeriodDTO dto : periodDTOs) {
                periodService.createPeriod(dto);
                successCount++;
            }

            redirectAttributes.addFlashAttribute("successMessage", 
                    "Nhập thành công " + successCount + " đợt đăng ký từ file Excel!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nhập Excel: " + ex.getMessage());
        }
        return "redirect:/admin/periods";
    }

}
