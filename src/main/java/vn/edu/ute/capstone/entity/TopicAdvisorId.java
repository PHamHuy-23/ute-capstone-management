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
public class TopicAdvisorId implements Serializable {

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "lecturer_id")
    private Long lecturerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicAdvisorId that)) return false;
        return Objects.equals(topicId, that.topicId) && Objects.equals(lecturerId, that.lecturerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId, lecturerId);
    }
}
