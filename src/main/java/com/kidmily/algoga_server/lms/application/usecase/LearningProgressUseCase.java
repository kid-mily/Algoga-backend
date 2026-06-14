package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.UpdateLearningProgressCommand;
import com.kidmily.algoga_server.lms.application.result.LearningProgressResult;

public interface LearningProgressUseCase {

    LearningProgressResult updateProgress(UpdateLearningProgressCommand command);
}