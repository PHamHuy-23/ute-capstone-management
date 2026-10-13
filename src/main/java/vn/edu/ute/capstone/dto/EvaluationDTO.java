package vn.edu.ute.capstone.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.edu.ute.capstone.entity.EvaluationType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationDTO {

    @NotNull(message = "ID đề tài không được để trống")
    private Long topicId;

    private Long councilId;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.0", message = "Điểm số tối thiểu là 0.0")
    @DecimalMax(value = "10.0", message = "Điểm số tối đa là 10.0")
    private BigDecimal score;

    private String comments;

    @NotNull(message = "Loại đánh giá không được để trống")
    private EvaluationType evaluationType;

}
