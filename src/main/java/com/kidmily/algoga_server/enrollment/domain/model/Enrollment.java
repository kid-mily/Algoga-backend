package com.kidmily.algoga_server.enrollment.domain.model;

import java.time.LocalDateTime;

public class Enrollment {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private EnrollmentStatus status;
    private final LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private final LocalDateTime accessExpiresAt;

    private Enrollment(
            Long id,
            Long userId,
            Long courseId,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime completedAt,
            LocalDateTime accessExpiresAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.status = status;
        this.enrolledAt = enrolledAt;
        this.completedAt = completedAt;
        this.accessExpiresAt = accessExpiresAt;
    }

    public static Enrollment create(Long userId, Long courseId) {
        return create(userId, courseId, LocalDateTime.now());
    }

    public static Enrollment create(Long userId, Long courseId, LocalDateTime enrolledAt) {
        return new Enrollment(null, userId, courseId, EnrollmentStatus.ENROLLED, enrolledAt, null, enrolledAt.plusMonths(6));
    }

    public static Enrollment withId(
            Long id,
            Long userId,
            Long courseId,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime completedAt,
            LocalDateTime accessExpiresAt
    ) {
        return new Enrollment(id, userId, courseId, status, enrolledAt, completedAt, accessExpiresAt);
    }

    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCourseId() { return courseId; }
    public EnrollmentStatus getStatus() { return status; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getAccessExpiresAt() { return accessExpiresAt; }

    public boolean isAccessibleAt(LocalDateTime now) {
        return accessExpiresAt == null || !accessExpiresAt.isBefore(now);
    }
}
