package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.CourseReview;

import java.time.LocalDateTime;

public record AdminCourseReviewResult(
        Long reviewId,
        Long courseId,
        Long userId,
        int rating,
        String content,
        boolean hidden,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminCourseReviewResult from(CourseReview review) {
        return new AdminCourseReviewResult(
                review.getId(),
                review.getCourseId(),
                review.getUserId(),
                review.getRating(),
                review.getContent(),
                review.isDeleted(),
                review.getDeletedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
