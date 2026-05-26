package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateQuizCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateQuizCommand;
import com.kidmily.algoga_server.lms.domain.model.Quiz;

import java.util.List;

public interface AdminQuizUseCase {

    List<Quiz> getQuizzes(Long courseId);

    Quiz createQuiz(CreateQuizCommand command);

    Quiz updateQuiz(Long courseId, Long quizId, UpdateQuizCommand command);

    void deleteQuiz(Long courseId, Long quizId);
}