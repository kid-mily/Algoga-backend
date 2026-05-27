package com.kidmily.algoga_server.lms.application.command;

public record CreateCourseQnaCommentCommand(
        Long courseId,
        Long qnaId,
        Long writerId,
        String writerType,
        String content
) {
}