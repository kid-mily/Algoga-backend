package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseQnaDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "강의 Q&A 상세 응답")
public record CourseQnaDetailResponse(

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

        @Schema(description = "답변 내용", example = "오사카 시내 관광 위주라면 오사카 주유패스를 추천드립니다.")
        String answer,

        @Schema(description = "Q&A 상태", example = "ANSWERED")
        String status,

        @Schema(description = "질문 작성일시", example = "2026-05-26T15:00:00")
        LocalDateTime createdAt,

        @Schema(description = "답변 작성일시", example = "2026-05-26T16:00:00")
        LocalDateTime answeredAt,

        @Schema(description = "댓글 목록")
        List<CourseQnaCommentResponse> comments
) {

    public static CourseQnaDetailResponse from(CourseQnaDetailResult result) {
        return new CourseQnaDetailResponse(
                result.qnaId(),
                result.courseId(),
                result.userId(),
                result.managerId(),
                result.title(),
                result.question(),
                result.answer(),
                result.status(),
                result.createdAt(),
                result.answeredAt(),
                result.comments()
                        .stream()
                        .map(CourseQnaCommentResponse::from)
                        .toList()
        );
    }
}