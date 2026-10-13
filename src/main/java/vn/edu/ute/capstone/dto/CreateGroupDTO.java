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
public class CreateGroupDTO {

    @NotBlank(message = "Mã nhóm không được để trống")
    @Size(max = 50, message = "Mã nhóm tối đa 50 ký tự")
    private String groupCode;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long periodId;

}
