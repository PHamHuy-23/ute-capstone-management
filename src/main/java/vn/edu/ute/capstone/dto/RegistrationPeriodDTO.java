package vn.edu.ute.capstone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import vn.edu.ute.capstone.entity.PeriodStatus;
import vn.edu.ute.capstone.entity.PeriodType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationPeriodDTO {

    private Long id;

    @NotBlank(message = "Tên đợt đăng ký không được để trống")
    private String name;

    @NotNull(message = "Vui lòng chọn loại đợt")
    private PeriodType periodType;

    @NotNull(message = "Thời gian bắt đầu cho GV đăng ký không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime lecturerStartDate;

    @NotNull(message = "Thời gian kết thúc cho GV đăng ký không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime lecturerEndDate;

    @NotNull(message = "Thời gian bắt đầu cho SV đăng ký không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime studentStartDate;

    @NotNull(message = "Thời gian kết thúc cho SV đăng ký không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime studentEndDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime reviewDeadline; // Chỉ bắt buộc khi là TLCN hoặc KLTN

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime defenseDate;    // Chỉ bắt buộc khi là KLTN

    private PeriodStatus status;

    private Integer minCredits;

    private java.math.BigDecimal minGpa;

    private Long targetDepartmentId;

    private String targetCohort;

    @Builder.Default
    private Boolean requiresAdvisorApproval = false;

}
