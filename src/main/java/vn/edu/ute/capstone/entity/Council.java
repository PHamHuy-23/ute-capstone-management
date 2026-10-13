package vn.edu.ute.capstone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "councils")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Council {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private RegistrationPeriod period;

    @Column(name = "defense_date")
    private LocalDateTime defenseDate;

    @Column(length = 255)
    private String location;

    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CouncilMember> members = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "council_topic_assignments",
        joinColumns = @JoinColumn(name = "council_id"),
        inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @Builder.Default
    private Set<Topic> assignedTopics = new HashSet<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}
