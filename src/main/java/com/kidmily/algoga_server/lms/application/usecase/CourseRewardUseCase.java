package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.lms.domain.model.CourseReward;

public interface CourseRewardUseCase {

    CourseReward rewardCourse(RewardCourseCommand command);
}