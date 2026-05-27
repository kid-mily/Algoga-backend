package com.kidmily.algoga_server.lms.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "어드민 강의 수정 요청")
public record UpdateCourseRequest(

        @Schema(description = "수정할 강의 제목", example = "오사카 여행 준비 마스터 개정판")
        @NotBlank(message = "강의 제목은 필수입니다.")
        String title,

        @Schema(description = "수정할 강의 설명", example = "오사카 여행 전 반드시 알아야 하는 교통, 환전, 입국 정보를 학습합니다.")
        @NotBlank(message = "강의 설명은 필수입니다.")
        String description,

        @Schema(description = "수정할 강의 가격", example = "120000")
        @NotNull(message = "강의 가격은 필수입니다.")
        @Positive(message = "강의 가격은 0보다 커야 합니다.")
        Integer price,

        @Schema(
                description = "강의 난이도. BEGINNER=초급, INTERMEDIATE=중급, ADVANCED=고급",
                example = "INTERMEDIATE",
                allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"}
        )
        @NotBlank(message = "강의 난이도는 필수입니다.")
        String level
) {
}