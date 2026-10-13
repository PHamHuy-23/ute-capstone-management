package vn.edu.ute.capstone.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "council_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilMember {

    @EmbeddedId
    private CouncilMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("councilId")
    @JoinColumn(name = "council_id", nullable = false)
    private Council council;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lecturerId")
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_council", nullable = false)
    private CouncilRole roleInCouncil;

}
