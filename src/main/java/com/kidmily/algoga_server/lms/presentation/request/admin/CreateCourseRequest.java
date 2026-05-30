package com.kidmily.algoga_server.lms.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "어드민 강의 생성 요청")
public record CreateCourseRequest(

        @Schema(description = "국가 ID", example = "1")
        @NotNull(message = "국가 ID는 필수입니다.")
        Long countryId,

        @Schema(description = "강의 제목", example = "오사카 여행 준비 마스터")
        @NotBlank(message = "강의 제목은 필수입니다.")
        String title,

        @Schema(description = "강의 설명", example = "환전부터 교통패스까지 오사카 여행 준비에 필요한 내용을 학습합니다.")
        @NotBlank(message = "강의 설명은 필수입니다.")
        String description,

        @Schema(description = "강의 가격", example = "100000")
        @NotNull(message = "강의 가격은 필수입니다.")
        @Positive(message = "강의 가격은 0보다 커야 합니다.")
        Integer price,

        @Schema(
                description = "강의 난이도. BEGINNER=초급, INTERMEDIATE=중급, ADVANCED=고급",
                example = "BEGINNER",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"}
        )
        @NotBlank(message = "강의 난이도는 필수입니다.")
        String level,

        @Schema(
                description = "강의 공개 상태. PUBLISHED=공개, DRAFT=비공개",
                example = "PUBLISHED",
                allowableValues = {"PUBLISHED", "DRAFT"}
        )
        String status
) {
}
