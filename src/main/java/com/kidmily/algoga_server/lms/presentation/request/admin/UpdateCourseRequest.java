package com.kidmily.algoga_server.lms.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "어드민 강의 수정 요청")
public record UpdateCourseRequest(

        @Schema(description = "수정할 강의 제목", example = "오사카 여행 준비 마스터 개정판")
        @NotBlank(message = "강의 제목은 필수입니다.")
        String title,

        @Schema(description = "수정할 강의 설명", example = "오사카 여행 전 반드시 알아야 하는 교통, 환전, 입국 정보를 학습합니다.")
        @NotBlank(message = "강의 설명은 필수입니다.")
        String description
) {
}