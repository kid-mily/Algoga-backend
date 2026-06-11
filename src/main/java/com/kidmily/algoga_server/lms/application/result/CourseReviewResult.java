package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.CourseReview;

import java.time.LocalDateTime;

public record CourseReviewResult(
        Long reviewId,
        Long courseId,
        Long userId,
        int rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CourseReviewResult from(CourseReview review) {
        return new CourseReviewResult(
                review.getId(),
                review.getCourseId(),
                review.getUserId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}