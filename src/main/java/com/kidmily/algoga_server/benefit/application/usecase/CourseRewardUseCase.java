package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.benefit.application.result.CourseRewardResult;

public interface CourseRewardUseCase {

    CourseRewardResult rewardCourse(RewardCourseCommand command);

    CourseRewardResult rewardCourseWithDetails(RewardCourseCommand command);
}
