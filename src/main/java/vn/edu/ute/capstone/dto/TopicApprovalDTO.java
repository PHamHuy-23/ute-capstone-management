package vn.edu.ute.capstone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicApprovalDTO {

    @NotNull(message = "ID đề tài không được để trống")
    private Long topicId;

    @NotNull(message = "Hành động phê duyệt không được để trống")
    private Boolean approved; // true: Duyệt, false: Từ chối

    private String rejectionReason; // Bắt buộc khi approved == false

}
