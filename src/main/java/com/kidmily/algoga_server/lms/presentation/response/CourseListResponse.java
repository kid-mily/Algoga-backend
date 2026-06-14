package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "강의 목록 응답")
public record CourseListResponse(
        @Schema(description = "강의 ID", example = "57")
        Long courseId,

        @Schema(description = "국가 ID", example = "1")
        Long countryId,

        @Schema(description = "강의 제목", example = "오사카 여행 준비 마스터")
        String title,

        @Schema(description = "강의 설명")
        String description,

        @Schema(description = "강의 가격", example = "15000")
        Integer price,

        @Schema(description = "썸네일 URL")
        String thumbnailUrl,

        @Schema(description = "강의 자료 URL 목록")
        List<String> fileUrls,

        @Schema(description = "원본 파일명을 포함한 강의 자료 목록")
        List<CourseFileResponse> files,

        @Schema(description = "강의 난이도 코드", example = "BEGINNER")
        String level,

        @Schema(description = "강의 난이도 이름", example = "초급")
        String levelName,

        @Schema(description = "강의 상태", example = "PUBLISHED")
        String status,

        @Schema(description = "현재 사용자 수강 여부", example = "true")
        boolean enrolled,

        @Schema(description = "현재 사용자 결제 여부", example = "true")
        boolean paid
) {

    public static CourseListResponse from(CourseResult course) {
        return from(course, false, false);
    }

    public static CourseListResponse from(
            CourseResult course,
            boolean enrolled,
            boolean paid
    ) {
        return new CourseListResponse(
                course.courseId(),
                course.countryId(),
                course.title(),
                course.description(),
                course.price(),
                course.thumbnailUrl(),
                course.fileUrls(),
                course.files()
                        .stream()
                        .map(CourseFileResponse::from)
                        .toList(),
                course.level(),
                course.levelName(),
                course.status(),
                enrolled,
                paid
        );
    }
}
