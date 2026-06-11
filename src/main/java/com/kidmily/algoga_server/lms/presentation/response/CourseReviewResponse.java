package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseReviewResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Course review response")
public record CourseReviewResponse(
        Long reviewId,
        Long courseId,
        Long userId,
        int rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CourseReviewResponse from(CourseReviewResult review) {
        return new CourseReviewResponse(
                review.reviewId(),
                review.courseId(),
                review.userId(),
                review.rating(),
                review.content(),
                review.createdAt(),
                review.updatedAt()
        );
    }
}