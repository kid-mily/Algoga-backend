package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CourseCompletion {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final String certificateCode;
    private final LocalDateTime completedAt;

    private CourseCompletion(
            Long id,
            Long userId,
            Long courseId,
            String certificateCode,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.certificateCode = certificateCode;
        this.completedAt = completedAt;
    }

    public static CourseCompletion create(
            Long userId,
            Long courseId
    ) {
        return new CourseCompletion(
                null,
                userId,
                courseId,
                generateCertificateCode(),
                LocalDateTime.now()
        );
    }

    public static CourseCompletion withId(
            Long id,
            Long userId,
            Long courseId,
            String certificateCode,
            LocalDateTime completedAt
    ) {
        return new CourseCompletion(
                id,
                userId,
                courseId,
                certificateCode,
                completedAt
        );
    }

    private static String generateCertificateCode() {
        return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCertificateCode() {
        return certificateCode;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}