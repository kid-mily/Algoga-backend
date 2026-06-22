package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.CourseReview;

import java.time.LocalDateTime;

public record CourseReviewResult(
        Long reviewId,
        Long courseId,
        Long userId,
        String nickname,
        int rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CourseReviewResult from(CourseReview review) {
        return from(review, null);
    }

    public static CourseReviewResult from(CourseReview review, String nickname) {
        return new CourseReviewResult(
                review.getId(),
                review.getCourseId(),
                review.getUserId(),
                nickname,
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}