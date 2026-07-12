package com.kidmily.algoga_server.qna.presentation.response;

import com.kidmily.algoga_server.qna.application.result.CourseQnaCommentResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자 강의 Q&A 상세 응답")
public record AdminCourseQnaDetailResponse(
        @Schema(description = "Q&A ID", example = "1")
        Long qnaId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "작성자 PK", example = "1")
        Long userId,

        @Schema(description = "작성자 아이디", example = "user01")
        String username,

        @Schema(description = "작성자 이름", example = "김알고")
        String name,

        @Schema(description = "작성자 이메일", example = "user@test.com")
        String email,

        @Schema(description = "답변 매니저 ID", example = "3")
        Long managerId,

        @Schema(description = "질문 제목")
        String title,

        @Schema(description = "질문 내용")
        String question,

        @Schema(description = "답변 내용")
        String answer,

        @Schema(description = "Q&A 상태", example = "ANSWERED")
        String status,

        @Schema(description = "질문 작성 일시", example = "2026-06-11T17:30:00")
        LocalDateTime createdAt,

        @Schema(description = "답변 작성 일시", example = "2026-06-11T18:00:00")
        LocalDateTime answeredAt,

        @Schema(description = "댓글 목록")
        List<AdminCourseQnaCommentResponse> comments
) {
    public static AdminCourseQnaDetailResponse from(CourseQnaDetailResult result) {
        return new AdminCourseQnaDetailResponse(
                result.qnaId(),
                result.courseId(),
                result.userId(),
                result.username(),
                result.name(),
                result.email(),
                result.managerId(),
                result.title(),
                result.question(),
                result.answer(),
                result.status(),
                result.createdAt(),
                result.answeredAt(),
                toCommentTree(result.comments())
        );
    }

    private static List<AdminCourseQnaCommentResponse> toCommentTree(List<CourseQnaCommentResult> comments) {
        return comments.stream()
                .filter(comment -> comment.parentCommentId() == null)
                .map(parent -> AdminCourseQnaCommentResponse.from(
                        parent,
                        comments.stream()
                                .filter(reply -> parent.commentId().equals(reply.parentCommentId()))
                                .map(AdminCourseQnaCommentResponse::from)
                                .toList()
                ))
                .toList();
    }
}