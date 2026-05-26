package com.kidmily.algoga_server.lms.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "강의 Q&A 등록 요청")
public record CreateCourseQnaRequest(

        @Schema(description = "질문 제목", example = "오사카 교통패스 관련 질문입니다.")
        @NotBlank(message = "질문 제목은 필수입니다.")
        String title,

        @Schema(description = "질문 내용", example = "오사카 주유패스와 간사이 패스 중 어떤 것을 선택해야 하나요?")
        @NotBlank(message = "질문 내용은 필수입니다.")
        String question
) {
}