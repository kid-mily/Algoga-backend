package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateQuizCommand;
import com.kidmily.algoga_server.lms.application.command.SubmitQuizCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateQuizCommand;
import com.kidmily.algoga_server.lms.application.result.QuizResult;
import com.kidmily.algoga_server.lms.application.result.QuizSubmitResult;

import java.util.List;

public interface QuizUseCase {

    List<QuizResult> getQuizzes(Long courseId);

    List<QuizResult> getQuizzes(Long userId, Long courseId);

    QuizResult createQuiz(CreateQuizCommand command);

    QuizResult updateQuiz(Long courseId, Long quizId, UpdateQuizCommand command);

    void deleteQuiz(Long courseId, Long quizId);

    QuizSubmitResult submitQuiz(SubmitQuizCommand command);
}
