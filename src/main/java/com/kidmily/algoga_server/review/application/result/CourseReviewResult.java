package com.kidmily.algoga_server.review.application.result;

import com.kidmily.algoga_server.lms.application.port.UserProfilePort;
import com.kidmily.algoga_server.review.domain.model.CourseReview;

import java.time.LocalDateTime;

public record CourseReviewResult(
        Long reviewId,
        Long courseId,
        Long userId,
        String username,
        String name,
        String email,
        String nickname,
        int rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CourseReviewResult from(CourseReview review) {
        return from(review, null);
    }

    public static CourseReviewResult from(CourseReview review, UserProfilePort.UserProfile profile) {
        return new CourseReviewResult(
                review.getId(),
                review.getCourseId(),
                review.getUserId(),
                profile == null ? null : profile.username(),
                profile == null ? null : profile.name(),
                profile == null ? null : profile.email(),
                profile == null ? null : profile.nickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}