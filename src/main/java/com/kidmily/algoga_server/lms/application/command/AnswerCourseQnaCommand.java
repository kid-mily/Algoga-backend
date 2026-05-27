package com.kidmily.algoga_server.lms.application.command;

public record AnswerCourseQnaCommand(
        Long courseId,
        Long qnaId,
        Long managerId,
        String answer
) {
}