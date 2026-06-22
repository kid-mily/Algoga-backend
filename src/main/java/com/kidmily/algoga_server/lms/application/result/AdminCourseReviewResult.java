package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.CourseReview;

import java.time.LocalDateTime;

public record AdminCourseReviewResult(
        Long reviewId,
        Long courseId,
        Long userId,
        String nickname,
        int rating,
        String content,
        boolean hidden,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminCourseReviewResult from(CourseReview review) {
        return from(review, null);
    }

    public static AdminCourseReviewResult from(CourseReview review, String nickname) {
        return new AdminCourseReviewResult(
                review.getId(),
                review.getCourseId(),
                review.getUserId(),
                nickname,
                review.getRating(),
                review.getContent(),
                review.isDeleted(),
                review.getDeletedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}