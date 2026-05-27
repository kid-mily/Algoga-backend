package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.benefit.domain.model.CourseReward;

public interface CourseRewardUseCase {

    CourseReward rewardCourse(RewardCourseCommand command);
}