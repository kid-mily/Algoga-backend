package com.kidmily.algoga_server.lms.application.command;

public record CreateQuizCommand(
        Long courseId,
        String question,
        String option1,
        String option2,
        String option3,
        String option4,
        int correctOption,
        String explanation
) {
}