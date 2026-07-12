package com.kidmily.algoga_server.diagnosis.presentation.response;

import com.kidmily.algoga_server.course.presentation.response.CourseListResponse;
import com.kidmily.algoga_server.diagnosis.application.result.DiagnosisResultView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "진단평가 결과 응답")
public record DiagnosisResultResponse(

        @Schema(description = "진단평가 결과 ID", example = "1")
        Long resultId,

        @Schema(description = "사용자가 선택한 국가 ID", example = "1")
        Long countryId,

        @Schema(description = "국가명", example = "일본")
        String countryName,

        @Schema(description = "정답 개수", example = "3")
        Integer correctCount,

        @Schema(description = "전체 문항 수", example = "5")
        Integer totalCount,

        @Schema(description = "점수", example = "60")
        Integer score,

        @Schema(description = "강의 레벨 코드", example = "INTERMEDIATE")
        String level,

        @Schema(description = "강의 레벨 이름", example = "중급")
        String levelName,

        @Schema(description = "제출 일시")
        LocalDateTime submittedAt,

        @Schema(description = "문항별 채점 결과")
        List<DiagnosisAnswerResultResponse> answers,

        @Schema(description = "추천 강의 목록")
        List<CourseListResponse> recommendedCourses
) {
    public static DiagnosisResultResponse from(DiagnosisResultView result) {
        return new DiagnosisResultResponse(
                result.resultId(),
                result.countryId(),
                result.countryName(),
                result.correctCount(),
                result.totalCount(),
                result.score(),
                result.level(),
                result.levelName(),
                result.submittedAt(),
                result.answers().stream()
                        .map(DiagnosisAnswerResultResponse::from)
                        .toList(),
                result.recommendedCourses().stream()
                        .map(CourseListResponse::from)
                        .toList()
        );
    }
}