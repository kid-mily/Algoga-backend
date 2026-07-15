package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.RecordCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RetryCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardFailureResult;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardResult;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;

import java.util.List;

public interface CourseRewardUseCase {

    CourseRewardResult rewardCourse(RewardCourseCommand command);

    CourseRewardResult rewardCourseWithDetails(RewardCourseCommand command);

    void recordFailure(RecordCourseRewardFailureCommand command);

    List<CourseRewardFailureResult> getFailures(CourseRewardFailureStatus status);

    List<CourseRewardFailureResult> getRetryableFailures(int maxRetryCount);

    CourseRewardFailureResult retryFailure(RetryCourseRewardFailureCommand command);
}
