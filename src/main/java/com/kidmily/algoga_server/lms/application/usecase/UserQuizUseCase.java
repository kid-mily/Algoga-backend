package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.SubmitQuizCommand;
import com.kidmily.algoga_server.lms.application.result.QuizSubmitResult;
import com.kidmily.algoga_server.lms.domain.model.Quiz;

import java.util.List;

public interface UserQuizUseCase {

    List<Quiz> getQuizzes(Long userId, Long courseId);

    QuizSubmitResult submitQuiz(SubmitQuizCommand command);
}