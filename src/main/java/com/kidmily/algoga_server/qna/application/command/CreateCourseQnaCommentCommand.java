package com.kidmily.algoga_server.qna.application.command;

public record CreateCourseQnaCommentCommand(
        Long courseId,
        Long qnaId,
        Long parentCommentId,
        Long writerId,
        String writerType,
        String content
) {
}