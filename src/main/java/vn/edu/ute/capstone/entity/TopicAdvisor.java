package vn.edu.ute.capstone.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topic_advisors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicAdvisor {

    @EmbeddedId
    private TopicAdvisorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("topicId")
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lecturerId")
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = true;

}
