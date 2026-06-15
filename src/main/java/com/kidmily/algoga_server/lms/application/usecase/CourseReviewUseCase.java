package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateCourseReviewCommand;
import com.kidmily.algoga_server.lms.application.result.CourseReviewResult;
import com.kidmily.algoga_server.lms.application.result.CourseReviewSummaryResult;

import java.util.List;

public interface CourseReviewUseCase {

    CourseReviewResult createReview(CreateCourseReviewCommand command);

    List<CourseReviewResult> getReviews(Long courseId);

    CourseReviewSummaryResult getReviewSummary(Long courseId);
}