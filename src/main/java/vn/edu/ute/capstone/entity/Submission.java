package vn.edu.ute.capstone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(nullable = false)
    private String title;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    @Builder.Default
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "submitted_at", insertable = false, updatable = false)
    private LocalDateTime submittedAt;

}
