package vn.edu.ute.capstone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilCreateDTO {

    private Long id;

    @NotBlank(message = "Tên hội đồng không được để trống")
    @Size(max = 255, message = "Tên hội đồng tối đa 255 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long periodId;

    private LocalDateTime defenseDate;

    private String location;

    @NotNull(message = "Vui lòng chọn Chủ tịch hội đồng")
    private Long chairmanId;

    @NotNull(message = "Vui lòng chọn Thư ký hội đồng")
    private Long secretaryId;

    // Danh sách 1 - 3 thành viên hội đồng khác (để tổng từ 3 - 5 người)
    private List<Long> memberIds;

    // Danh sách đề tài phân công cho hội đồng này chấm
    private List<Long> assignedTopicIds;

}
