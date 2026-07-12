package com.kidmily.algoga_server.learningprogress.application.usecase;

import com.kidmily.algoga_server.learningprogress.application.command.UpdateLearningProgressCommand;
import com.kidmily.algoga_server.learningprogress.application.result.LearningProgressResult;

public interface LearningProgressUseCase {

    LearningProgressResult updateProgress(UpdateLearningProgressCommand command);
}