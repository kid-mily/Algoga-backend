package com.kidmily.algoga_server.review.application.result;

import com.kidmily.algoga_server.lms.application.port.UserProfilePort;
import com.kidmily.algoga_server.review.domain.model.CourseReview;

import java.time.LocalDateTime;

public record AdminCourseReviewResult(
        Long reviewId,
        Long courseId,
        Long userId,
        String username,
        String name,
        String email,
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

    public static AdminCourseReviewResult from(CourseReview review, UserProfilePort.UserProfile profile) {
        return new AdminCourseReviewResult(
                review.getId(),
                review.getCourseId(),
                review.getUserId(),
                profile == null ? null : profile.username(),
                profile == null ? null : profile.name(),
                profile == null ? null : profile.email(),
                review.getRating(),
                review.getContent(),
                review.isDeleted(),
                review.getDeletedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}