package vn.edu.ute.capstone.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String departmentName; // Tên bộ môn (nếu có)
    private Set<String> roles;     // Danh sách các vai trò (ROLE_ADMIN, ROLE_STUDENT...)

}
