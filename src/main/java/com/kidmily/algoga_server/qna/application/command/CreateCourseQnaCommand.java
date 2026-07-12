package com.kidmily.algoga_server.qna.application.command;

public record CreateCourseQnaCommand(
        Long courseId,
        Long userId,
        String title,
        String question
) {
}