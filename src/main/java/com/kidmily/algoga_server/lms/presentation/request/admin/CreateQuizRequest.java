package com.kidmily.algoga_server.lms.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "퀴즈 등록 요청")
public record CreateQuizRequest(

        @Schema(description = "퀴즈 문제", example = "일본 오사카 여행 전 준비물로 가장 적절한 것은?")
        @NotBlank(message = "퀴즈 문제는 필수입니다.")
        String question,

        @Schema(description = "1번 보기", example = "여권")
        @NotBlank(message = "1번 보기는 필수입니다.")
        String option1,

        @Schema(description = "2번 보기", example = "두꺼운 겨울 패딩")
        @NotBlank(message = "2번 보기는 필수입니다.")
        String option2,

        @Schema(description = "3번 보기", example = "국제운전면허증만")
        @NotBlank(message = "3번 보기는 필수입니다.")
        String option3,

        @Schema(description = "4번 보기", example = "현지 주민등록증")
        @NotBlank(message = "4번 보기는 필수입니다.")
        String option4,

        @Schema(description = "정답 보기 번호. 1부터 4 사이", example = "1")
        @NotNull(message = "정답 번호는 필수입니다.")
        @Min(value = 1, message = "정답 번호는 1 이상이어야 합니다.")
        @Max(value = 4, message = "정답 번호는 4 이하이어야 합니다.")
        Integer correctOption,

        @Schema(description = "해설", example = "해외여행 시 여권은 필수 준비물입니다.")
        String explanation
) {
}