package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.UpdateLearningProgressCommand;
import com.kidmily.algoga_server.lms.domain.model.LearningProgress;

public interface LearningProgressUseCase {

    LearningProgress updateProgress(UpdateLearningProgressCommand command);
}