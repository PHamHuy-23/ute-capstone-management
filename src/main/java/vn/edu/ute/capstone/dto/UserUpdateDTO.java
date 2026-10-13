package vn.edu.ute.capstone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    private Long id;

    // Username thường là mã định danh không được sửa sau khi tạo, nhưng giữ để hiển thị
    private String username;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;

    private Long departmentId;

    private Boolean isActive;

    private Set<Long> roleIds;

}
