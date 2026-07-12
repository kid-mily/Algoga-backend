package com.kidmily.algoga_server.qna.application.command;

public record AnswerCourseQnaCommand(
        Long courseId,
        Long qnaId,
        Long managerId,
        String answer
) {
}