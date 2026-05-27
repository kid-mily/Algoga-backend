package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.CourseQnaComment;
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

        @Schema(description = "매니저 ID", example = "1")
        Long managerId,

        @Schema(description = "작성자 타입. USER 또는 MANAGER", example = "USER")
        String writerType,

        @Schema(description = "댓글 내용", example = "추가로 궁금한 점이 있습니다.")
        String content,

        @Schema(description = "댓글 작성일시", example = "2026-05-27T01:30:00")
        LocalDateTime createdAt
) {

    public static CourseQnaCommentResponse from(CourseQnaComment comment) {
        return new CourseQnaCommentResponse(
                comment.getId(),
                comment.getQnaId(),
                comment.getUserId(),
                comment.getManagerId(),
                comment.getWriterType(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}