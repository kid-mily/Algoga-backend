package com.kidmily.algoga_server.quiz.application.result;

import com.kidmily.algoga_server.quiz.domain.model.Quiz;

public record AdminQuizListItemResult(
        Long quizId,
        Long courseId,
        String courseTitle,
        String question,
        String option1,
        String option2,
        String option3,
        String option4,
        int correctOption,
        String explanation
) {
    public static AdminQuizListItemResult from(Quiz quiz, String courseTitle) {
        return new AdminQuizListItemResult(
                quiz.getId(),
                quiz.getCourseId(),
                courseTitle,
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
