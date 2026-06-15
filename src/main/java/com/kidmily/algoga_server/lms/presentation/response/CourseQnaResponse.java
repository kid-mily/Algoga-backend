package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseQnaResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의 Q&A 응답")
public record CourseQnaResponse(
        @Schema(description = "Q&A ID", example = "1")
        Long qnaId,

        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "질문 작성자 ID", example = "1")
        Long userId,

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
        LocalDateTime answeredAt
) {
    public static CourseQnaResponse from(CourseQnaResult qna) {
        return new CourseQnaResponse(
                qna.qnaId(),
                qna.courseId(),
                qna.userId(),
                qna.managerId(),
                qna.title(),
                qna.question(),
                qna.answer(),
                qna.status(),
                qna.createdAt(),
                qna.answeredAt()
        );
    }
}
