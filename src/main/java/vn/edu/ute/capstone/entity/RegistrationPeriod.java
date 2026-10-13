package vn.edu.ute.capstone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @Column(name = "lecturer_start_date", nullable = false)
    private LocalDateTime lecturerStartDate;

    @Column(name = "lecturer_end_date", nullable = false)
    private LocalDateTime lecturerEndDate;

    @Column(name = "student_start_date", nullable = false)
    private LocalDateTime studentStartDate;

    @Column(name = "student_end_date", nullable = false)
    private LocalDateTime studentEndDate;

    @Column(name = "review_deadline")
    private LocalDateTime reviewDeadline;

    @Column(name = "defense_date")
    private LocalDateTime defenseDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PeriodStatus status = PeriodStatus.DRAFT;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}
