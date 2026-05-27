package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.lms.application.result.CourseReviewSummaryResult;
import com.kidmily.algoga_server.lms.domain.model.CourseReview;

import java.util.List;

public interface CourseReviewUseCase {

    CourseReview createReview(CreateCourseReviewCommand command);

    List<CourseReview> getReviews(Long courseId);

    CourseReviewSummaryResult getReviewSummary(Long courseId);
}