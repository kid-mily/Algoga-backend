package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public class Enrollment {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private EnrollmentStatus status;
    private final LocalDateTime enrolledAt;
    private LocalDateTime completedAt;

    private Enrollment(
            Long id,
            Long userId,
            Long courseId,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.status = status;
        this.enrolledAt = enrolledAt;
        this.completedAt = completedAt;
    }

    public static Enrollment create(Long userId, Long courseId) {
        return new Enrollment(null, userId, courseId, EnrollmentStatus.ENROLLED, LocalDateTime.now(), null);
    }

    public static Enrollment withId(
            Long id,
            Long userId,
            Long courseId,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime completedAt
    ) {
        return new Enrollment(id, userId, courseId, status, enrolledAt, completedAt);
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
}