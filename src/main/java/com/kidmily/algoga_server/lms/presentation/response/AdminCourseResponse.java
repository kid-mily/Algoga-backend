package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.Course;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "어드민 강의 응답")
public record AdminCourseResponse(

        @Schema(description = "강의 ID", example = "1")
        Long courseId,

        @Schema(description = "국가 ID", example = "1")
        Long countryId,

        @Schema(description = "콘텐츠 매니저 ID", example = "1")
        Long managerId,

        @Schema(description = "강의 제목", example = "오사카 여행 준비 마스터")
        String title,

        @Schema(description = "강의 설명", example = "환전부터 교통패스까지 오사카 여행 준비에 필요한 내용을 학습합니다.")
        String description,

        @Schema(description = "강의 가격", example = "100000")
        Integer price,

        @Schema(description = "썸네일 URL")
        String thumbnailUrl,

        @Schema(description = "첨부파일 경로")
        String fileUrl,

        @Schema(description = "강의 난이도 코드", example = "BEGINNER")
        String level,

        @Schema(description = "강의 난이도 이름", example = "초급")
        String levelName,

        @Schema(description = "강의 상태", example = "DRAFT")
        String status
) {

    public static AdminCourseResponse from(Course course) {
        return new AdminCourseResponse(
                course.getId(),
                course.getCountryId(),
                course.getManagerId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getThumbnailUrl(),
                course.getFileUrl(),
                course.getLevel(),
                toLevelName(course.getLevel()),
                course.getStatus()
        );
    }

    private static String toLevelName(String level) {
        return switch (level) {
            case "BEGINNER" -> "초급";
            case "INTERMEDIATE" -> "중급";
            case "ADVANCED" -> "고급";
            default -> "";
        };
    }
}