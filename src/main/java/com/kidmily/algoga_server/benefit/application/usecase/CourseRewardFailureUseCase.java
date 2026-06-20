package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.RecordCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RetryCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardFailureResult;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;

import java.util.List;

public interface CourseRewardFailureUseCase {

    void recordFailure(RecordCourseRewardFailureCommand command);

    List<CourseRewardFailureResult> getFailures(CourseRewardFailureStatus status);

    List<CourseRewardFailureResult> getRetryableFailures(int maxRetryCount);

    CourseRewardFailureResult retryFailure(RetryCourseRewardFailureCommand command);
}