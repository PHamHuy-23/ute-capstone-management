package vn.edu.ute.capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMemberId implements Serializable {

    @Column(name = "council_id")
    private Long councilId;

    @Column(name = "lecturer_id")
    private Long lecturerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CouncilMemberId that)) return false;
        return Objects.equals(councilId, that.councilId) && Objects.equals(lecturerId, that.lecturerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(councilId, lecturerId);
    }
}
