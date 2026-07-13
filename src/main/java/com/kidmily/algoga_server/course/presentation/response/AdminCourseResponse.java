package com.kidmily.algoga_server.course.presentation.response;

import com.kidmily.algoga_server.course.application.result.CourseResult;
import com.kidmily.algoga_server.global.infrastructure.s3.CdnMappable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "관리자 강의 응답")
public record AdminCourseResponse(
        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "국가 ID", example = "1")
        Long countryId,

        @Schema(description = "담당 매니저 ID", example = "3")
        Long managerId,

        @Schema(description = "강의 제목", example = "오사카 여행 준비 마스터")
        String title,

        @Schema(description = "강의 설명")
        String description,

        @Schema(description = "강의 가격", example = "15000")
        Integer price,

        @Schema(description = "강의 썸네일 URL")
        String thumbnailUrl,

        @Schema(description = "강의 자료 URL 목록")
        List<String> fileUrls,

        @Schema(description = "원본 파일명을 포함한 강의 자료 목록")
        List<CourseFileResponse> files,

        @Schema(description = "강의 레벨 코드", example = "BEGINNER")
        String level,

        @Schema(description = "강의 레벨 이름", example = "초급")
        String levelName,

        @Schema(description = "강의 상태", example = "PUBLISHED")
        String status,

        @Schema(description = "삭제 여부", example = "true")
        boolean deleted
) implements CdnMappable {
    public static AdminCourseResponse from(CourseResult course) {
        return new AdminCourseResponse(
                course.courseId(),
                course.countryId(),
                course.managerId(),
                course.title(),
                course.description(),
                course.price(),
                course.thumbnailUrl(),
                course.fileUrls(),
                course.files().stream().map(CourseFileResponse::from).toList(),
                course.level(),
                course.levelName(),
                course.status(),
                course.deleted()
        );
    }
}