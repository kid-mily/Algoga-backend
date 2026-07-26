package com.kidmily.algoga_server.review.application.result;

import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.review.domain.model.CourseReview;

import java.time.LocalDateTime;

public record AdminCourseReviewListItemResult(
        Long reviewId,
        Long courseId,
        String courseTitle,
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
    public static AdminCourseReviewListItemResult from(
            CourseReview review,
            String courseTitle,
            UserProfilePort.UserProfile profile
    ) {
        return new AdminCourseReviewListItemResult(
                review.getId(),
                review.getCourseId(),
                courseTitle,
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
