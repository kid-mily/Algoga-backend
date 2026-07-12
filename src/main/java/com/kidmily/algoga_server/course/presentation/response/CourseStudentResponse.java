package com.kidmily.algoga_server.course.presentation.response;

import com.kidmily.algoga_server.course.application.result.CourseStudentResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 강의 수강생 응답")
public record CourseStudentResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자 이름", example = "윤라프")
        String name,

        @Schema(description = "사용자 이메일", example = "testuser88@algoga.com")
        String email,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "강의 제목", example = "오사카 여행 준비 마스터")
        String courseTitle,

        @Schema(description = "전체 진도율", example = "100")
        int progressRate,

        @Schema(description = "완료한 챕터 수", example = "4")
        int completedChapterCount,

        @Schema(description = "전체 챕터 수", example = "4")
        int totalChapterCount,

        @Schema(description = "학습 상태. IN_PROGRESS 또는 COMPLETED", example = "COMPLETED")
        String learningStatus,

        @Schema(description = "퀴즈 제출 여부", example = "true")
        boolean quizSubmitted,

        @Schema(description = "리뷰 작성 여부", example = "false")
        boolean reviewWritten,

        @Schema(description = "수강 가능 만료 일시", example = "2026-12-10T15:30:00")
        LocalDateTime accessExpiresAt,

        @Schema(description = "수료 일시", example = "2026-05-26T15:30:00")
        LocalDateTime completedAt
) {

    public static CourseStudentResponse from(CourseStudentResult result) {
        return new CourseStudentResponse(
                result.userId(),
                result.name(),
                result.email(),
                result.courseId(),
                result.courseTitle(),
                result.progressRate(),
                result.completedChapterCount(),
                result.totalChapterCount(),
                result.learningStatus(),
                result.quizSubmitted(),
                result.reviewWritten(),
                result.accessExpiresAt(),
                result.completedAt()
        );
    }
}
