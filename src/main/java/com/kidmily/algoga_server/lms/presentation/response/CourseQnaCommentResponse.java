package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseQnaCommentResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의 Q&A 댓글 응답")
public record CourseQnaCommentResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "Q&A ID", example = "1")
        Long qnaId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "매니저 ID", example = "3")
        Long managerId,

        @Schema(description = "작성자 타입", example = "USER")
        String writerType,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "작성 일시", example = "2026-06-11T17:30:00")
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
