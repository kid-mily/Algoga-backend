package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.CourseQnaComment;

import java.time.LocalDateTime;

public record CourseQnaCommentResult(
        Long commentId,
        Long qnaId,
        Long userId,
        Long managerId,
        String writerType,
        String nickname,
        String content,
        LocalDateTime createdAt
) {
    public static CourseQnaCommentResult from(CourseQnaComment comment) {
        return from(comment, null);
    }

    public static CourseQnaCommentResult from(CourseQnaComment comment, String nickname) {
        return new CourseQnaCommentResult(
                comment.getId(),
                comment.getQnaId(),
                comment.getUserId(),
                comment.getManagerId(),
                comment.getWriterType(),
                nickname,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}