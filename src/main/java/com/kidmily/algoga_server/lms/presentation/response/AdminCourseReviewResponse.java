package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.AdminCourseReviewResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Admin course review response")
public record AdminCourseReviewResponse(
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
    public static AdminCourseReviewResponse from(AdminCourseReviewResult review) {
        return new AdminCourseReviewResponse(
                review.reviewId(),
                review.courseId(),
                review.userId(),
                review.rating(),
                review.content(),
                review.hidden(),
                review.deletedAt(),
                review.createdAt(),
                review.updatedAt()
        );
    }
}
