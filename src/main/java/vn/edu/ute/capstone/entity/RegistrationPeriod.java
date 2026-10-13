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

    @Column(name = "min_credits")
    @Builder.Default
    private Integer minCredits = 0;

    @Column(name = "min_gpa", precision = 3, scale = 2)
    @Builder.Default
    private java.math.BigDecimal minGpa = java.math.BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id")
    private Department targetDepartment;

    @Column(name = "target_cohort", length = 100)
    private String targetCohort;

    @Column(name = "requires_advisor_approval")
    @Builder.Default
    private Boolean requiresAdvisorApproval = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}
