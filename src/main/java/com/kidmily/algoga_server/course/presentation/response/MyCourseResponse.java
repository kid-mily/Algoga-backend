package com.kidmily.algoga_server.course.presentation.response;

import com.kidmily.algoga_server.course.application.result.MyCourseResult;
import com.kidmily.algoga_server.global.infrastructure.s3.CdnMappable;
import com.kidmily.algoga_server.global.infrastructure.s3.NoCdnUrl;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 수강 강의 응답")
public record MyCourseResponse(

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "강의 제목", example = "오사카 여행 준비 마스터")
        String title,

        @Schema(description = "썸네일 URL", example = "thumbnails/sample.png")
        String thumbnailUrl,

        @Schema(description = "국가 ID", example = "1")
        Long countryId,

        @Schema(description = "대륙 코드", example = "ASIA")
        String continentCode,

        @Schema(description = "국가명", example = "일본")
        String countryName,

        @Schema(description = "총 강의 시간. 초 단위", example = "12600")
        int totalDurationSeconds,

        @Schema(description = "수강생 수", example = "1234")
        long studentCount,

        @Schema(description = "평균 별점", example = "4.8")
        double averageRating,

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

        @Schema(description = "리뷰 작성 여부", example = "true")
        boolean reviewWritten,

        @Schema(description = "수료증 보기 버튼 노출 가능 여부", example = "true")
        boolean certificateAvailable,

        @Schema(description = "수료 코드", example = "ALG-2026-CERT-123456")
        String certificateCode,

        @Schema(description = "수료증 PDF 다운로드 API URL", example = "/api/v1/courses/3/certificate")
        @NoCdnUrl
        String certificateDownloadUrl,

        @Schema(description = "수강 가능 만료 일시", example = "2026-12-10T15:30:00")
        LocalDateTime accessExpiresAt,

        @Schema(description = "수료 일시", example = "2026-05-26T15:30:00")
        LocalDateTime completedAt
) implements CdnMappable {

    public static MyCourseResponse from(MyCourseResult result) {
        return new MyCourseResponse(
                result.courseId(),
                result.title(),
                result.thumbnailUrl(),
                result.countryId(),
                result.continentCode(),
                result.countryName(),
                result.totalDurationSeconds(),
                result.studentCount(),
                result.averageRating(),
                result.progressRate(),
                result.completedChapterCount(),
                result.totalChapterCount(),
                result.learningStatus(),
                result.quizSubmitted(),
                result.reviewWritten(),
                result.certificateAvailable(),
                result.certificateCode(),
                result.certificateDownloadUrl(),
                result.accessExpiresAt(),
                result.completedAt()
        );
    }
}
