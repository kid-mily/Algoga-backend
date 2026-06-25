package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.QuizSubmissionAnswer;

public record QuizSubmissionAnswerResult(
        Long answerId,
        Long quizId,
        String question,
        String option1,
        String option2,
        String option3,
        String option4,
        int selectedOption,
        String selectedAnswer,
        int correctOption,
        String correctAnswer,
        boolean correct,
        String explanation
) {
    public static QuizSubmissionAnswerResult from(QuizSubmissionAnswer answer) {
        return new QuizSubmissionAnswerResult(
                answer.getId(),
                answer.getQuizId(),
                answer.getQuestion(),
                answer.getOption1(),
                answer.getOption2(),
                answer.getOption3(),
                answer.getOption4(),
                answer.getSelectedOption(),
                answer.getSelectedAnswer(),
                answer.getCorrectOption(),
                answer.getCorrectAnswer(),
                answer.isCorrect(),
                answer.getExplanation()
        );
    }
}