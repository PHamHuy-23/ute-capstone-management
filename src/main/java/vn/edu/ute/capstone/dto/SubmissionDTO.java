package vn.edu.ute.capstone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDTO {

    @NotNull(message = "ID nhóm không được để trống")
    private Long groupId;

    @NotBlank(message = "Tiêu đề báo cáo không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    private String notes;

    private MultipartFile file;

}
