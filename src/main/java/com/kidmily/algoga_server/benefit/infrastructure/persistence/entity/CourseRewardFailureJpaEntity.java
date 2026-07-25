package com.kidmily.algoga_server.benefit.infrastructure.persistence.entity;

import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_reward_failures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRewardFailureJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failure_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "completion_id", nullable = false)
    private Long completionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CourseRewardFailureStatus status;

    @Lob
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_failed_at", nullable = false)
    private LocalDateTime lastFailedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public CourseRewardFailureJpaEntity(
            Long userId,
            Long courseId,
            Long completionId,
            CourseRewardFailureStatus status,
            String failureReason,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime lastFailedAt,
            LocalDateTime resolvedAt
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.completionId = completionId;
        this.status = status;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.lastFailedAt = lastFailedAt;
        this.resolvedAt = resolvedAt;
    }

    public void update(
            CourseRewardFailureStatus status,
            String failureReason,
            int retryCount,
            LocalDateTime lastFailedAt,
            LocalDateTime resolvedAt
    ) {
        this.status = status;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.lastFailedAt = lastFailedAt;
        this.resolvedAt = resolvedAt;
    }
}
