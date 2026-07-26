package com.kidmily.algoga_server.review.application.usecase;

import com.kidmily.algoga_server.review.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.review.application.command.UpdateCourseReviewVisibilityCommand;
import com.kidmily.algoga_server.review.application.result.AdminCourseReviewListItemResult;
import com.kidmily.algoga_server.review.application.result.AdminCourseReviewResult;
import com.kidmily.algoga_server.review.application.result.CourseReviewResult;
import com.kidmily.algoga_server.review.application.result.CourseReviewSummaryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseReviewUseCase {

    CourseReviewResult createReview(CreateCourseReviewCommand command);

    Page<CourseReviewResult> getReviews(Long courseId, Pageable pageable);

    CourseReviewSummaryResult getReviewSummary(Long courseId);

    Page<AdminCourseReviewResult> getAdminReviews(Long courseId, Pageable pageable);

    Page<AdminCourseReviewListItemResult> getAdminReviewList(
            Long courseId,
            Integer rating,
            Boolean hidden,
            String keyword,
            Pageable pageable
    );

    AdminCourseReviewResult getAdminReview(Long courseId, Long reviewId);

    AdminCourseReviewResult updateReviewVisibility(UpdateCourseReviewVisibilityCommand command);

    void deleteReview(Long courseId, Long reviewId);
}
