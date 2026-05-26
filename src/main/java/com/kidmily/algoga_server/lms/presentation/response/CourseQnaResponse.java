package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.CourseQna;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "강의 Q&A 응답")
public record CourseQnaResponse(

        @Schema(description = "Q&A ID", example = "1")
        Long qnaId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "질문 작성자 ID", example = "1")
        Long userId,

        @Schema(description = "답변 작성 매니저 ID", example = "1")
        Long managerId,

        @Schema(description = "질문 제목", example = "오사카 교통패스 관련 질문입니다.")
        String title,

        @Schema(description = "질문 내용", example = "오사카 주유패스와 간사이 패스 중 어떤 것을 선택해야 하나요?")
        String question,

        @Schema(description = "답변 내용", example = "오사카 시내 위주 일정이라면 오사카 주유패스를 추천드립니다.")
        String answer,

        @Schema(description = "Q&A 상태", example = "WAITING")
        String status,

        @Schema(description = "질문 작성일시", example = "2026-05-26T15:00:00")
        LocalDateTime createdAt,

        @Schema(description = "답변 작성일시", example = "2026-05-26T16:00:00")
        LocalDateTime answeredAt
) {

    public static CourseQnaResponse from(CourseQna qna) {
        return new CourseQnaResponse(
                qna.getId(),
                qna.getCourseId(),
                qna.getUserId(),
                qna.getManagerId(),
                qna.getTitle(),
                qna.getQuestion(),
                qna.getAnswer(),
                qna.getStatus(),
                qna.getCreatedAt(),
                qna.getAnsweredAt()
        );
    }
}