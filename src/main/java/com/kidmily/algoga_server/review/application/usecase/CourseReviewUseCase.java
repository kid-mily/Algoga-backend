package com.kidmily.algoga_server.review.application.usecase;

import com.kidmily.algoga_server.review.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.review.application.command.UpdateCourseReviewVisibilityCommand;
import com.kidmily.algoga_server.review.application.result.AdminCourseReviewResult;
import com.kidmily.algoga_server.review.application.result.CourseReviewResult;
import com.kidmily.algoga_server.review.application.result.CourseReviewSummaryResult;

import java.util.List;

public interface CourseReviewUseCase {

    CourseReviewResult createReview(CreateCourseReviewCommand command);

    List<CourseReviewResult> getReviews(Long courseId);

    CourseReviewSummaryResult getReviewSummary(Long courseId);

    List<AdminCourseReviewResult> getAdminReviews(Long courseId);

    AdminCourseReviewResult getAdminReview(Long courseId, Long reviewId);

    AdminCourseReviewResult updateReviewVisibility(UpdateCourseReviewVisibilityCommand command);

    void deleteReview(Long courseId, Long reviewId);
}
