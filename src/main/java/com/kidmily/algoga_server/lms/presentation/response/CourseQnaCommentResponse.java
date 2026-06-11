package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseQnaCommentResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Course Q&A comment response")
public record CourseQnaCommentResponse(
        Long commentId,
        Long qnaId,
        Long userId,
        Long managerId,
        String writerType,
        String content,
        LocalDateTime createdAt
) {
    public static CourseQnaCommentResponse from(CourseQnaCommentResult comment) {
        return new CourseQnaCommentResponse(
                comment.commentId(),
                comment.qnaId(),
                comment.userId(),
                comment.managerId(),
                comment.writerType(),
                comment.content(),
                comment.createdAt()
        );
    }
}