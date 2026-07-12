package com.kidmily.algoga_server.qna.presentation.response;

import com.kidmily.algoga_server.qna.application.result.CourseQnaCommentResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자 강의 Q&A 댓글 응답")
public record AdminCourseQnaCommentResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long commentId,

        @Schema(description = "Q&A ID", example = "1")
        Long qnaId,

        @Schema(description = "부모 댓글 ID. 일반 댓글이면 null", example = "1")
        Long parentCommentId,

        @Schema(description = "사용자 PK", example = "1")
        Long userId,

        @Schema(description = "사용자 아이디", example = "user01")
        String username,

        @Schema(description = "사용자 이름", example = "김알고")
        String name,

        @Schema(description = "사용자 이메일", example = "user@test.com")
        String email,

        @Schema(description = "매니저 ID", example = "3")
        Long managerId,

        @Schema(description = "작성자 타입", example = "USER")
        String writerType,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "작성 일시", example = "2026-06-11T17:30:00")
        LocalDateTime createdAt,

        @Schema(description = "대댓글 목록")
        List<AdminCourseQnaCommentResponse> replies
) {
    public static AdminCourseQnaCommentResponse from(CourseQnaCommentResult comment) {
        return from(comment, List.of());
    }

    public static AdminCourseQnaCommentResponse from(
            CourseQnaCommentResult comment,
            List<AdminCourseQnaCommentResponse> replies
    ) {
        return new AdminCourseQnaCommentResponse(
                comment.commentId(),
                comment.qnaId(),
                comment.parentCommentId(),
                comment.userId(),
                comment.username(),
                comment.name(),
                comment.email(),
                comment.managerId(),
                comment.writerType(),
                comment.content(),
                comment.createdAt(),
                replies
        );
    }
}