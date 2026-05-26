package com.kidmily.algoga_server.lms.domain.model;

import java.time.LocalDateTime;

public class CourseReview {

    private final Long id;
    private final Long courseId;
    private final Long userId;
    private final int rating;
    private final String content;
    private final boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CourseReview(
            Long id,
            Long courseId,
            Long userId,
            int rating,
            String content,
            boolean deleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CourseReview create(
            Long courseId,
            Long userId,
            int rating,
            String content
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new CourseReview(
                null,
                courseId,
                userId,
                rating,
                content,
                false,
                now,
                now
        );
    }

    public static CourseReview withId(
            Long id,
            Long courseId,
            Long userId,
            int rating,
            String content,
            boolean deleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new CourseReview(
                id,
                courseId,
                userId,
                rating,
                content,
                deleted,
                createdAt,
                updatedAt
        );
    }

    public CourseReview update(
            int rating,
            String content
    ) {
        return new CourseReview(
                this.id,
                this.courseId,
                this.userId,
                rating,
                content,
                this.deleted,
                this.createdAt,
                LocalDateTime.now()
        );
    }

    public CourseReview delete() {
        return new CourseReview(
                this.id,
                this.courseId,
                this.userId,
                this.rating,
                this.content,
                true,
                this.createdAt,
                LocalDateTime.now()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getUserId() {
        return userId;
    }

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}