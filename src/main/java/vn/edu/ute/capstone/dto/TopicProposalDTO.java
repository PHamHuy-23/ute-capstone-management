package vn.edu.ute.capstone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicProposalDTO {

    private Long id;

    @NotBlank(message = "Mã đề tài không được để trống")
    @Size(max = 50, message = "Mã đề tài tối đa 50 ký tự")
    private String code;

    @NotBlank(message = "Tên đề tài không được để trống")
    @Size(max = 255, message = "Tên đề tài tối đa 255 ký tự")
    private String title;

    private String description;

    private String requirements;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long periodId;

    @NotNull(message = "Vui lòng chọn bộ môn quản lý")
    private Long departmentId;

    // Giảng viên đồng hướng dẫn (Tùy chọn, tối đa thêm 1 GV)
    private Long coLecturerId;

}
