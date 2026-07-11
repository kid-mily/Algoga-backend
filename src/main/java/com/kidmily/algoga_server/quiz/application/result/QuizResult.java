package com.kidmily.algoga_server.quiz.application.result;

import com.kidmily.algoga_server.quiz.domain.model.Quiz;

public record QuizResult(
        Long quizId,
        Long courseId,
        String question,
        String option1,
        String option2,
        String option3,
        String option4,
        int correctOption,
        String explanation
) {
    public static QuizResult from(Quiz quiz) {
        return new QuizResult(
                quiz.getId(),
                quiz.getCourseId(),
                quiz.getQuestion(),
                quiz.getOption1(),
                quiz.getOption2(),
                quiz.getOption3(),
                quiz.getOption4(),
                quiz.getCorrectOption(),
                quiz.getExplanation()
        );
    }
}