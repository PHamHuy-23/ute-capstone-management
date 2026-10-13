package vn.edu.ute.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEligibilityDTO {
    private boolean eligible;
    @Builder.Default
    private List<String> reasons = new ArrayList<>();

    public void addReason(String reason) {
        if (reasons == null) {
            reasons = new ArrayList<>();
        }
        reasons.add(reason);
    }
}
