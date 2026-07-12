package com.kidmily.algoga_server.quiz.presentation.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "퀴즈 수정 요청")
public record UpdateQuizRequest(

        @Schema(description = "수정할 퀴즈 문제", example = "오사카 여행 전 반드시 확인해야 하는 서류는?")
        @NotBlank(message = "퀴즈 문제는 필수입니다.")
        String question,

        @Schema(description = "수정할 1번 보기", example = "여권")
        @NotBlank(message = "1번 보기는 필수입니다.")
        String option1,

        @Schema(description = "수정할 2번 보기", example = "호텔 키")
        @NotBlank(message = "2번 보기는 필수입니다.")
        String option2,

        @Schema(description = "수정할 3번 보기", example = "국내 학생증")
        @NotBlank(message = "3번 보기는 필수입니다.")
        String option3,

        @Schema(description = "수정할 4번 보기", example = "주민센터 서류")
        @NotBlank(message = "4번 보기는 필수입니다.")
        String option4,

        @Schema(description = "수정할 정답 보기 번호. 1부터 4 사이", example = "1")
        @NotNull(message = "정답 번호는 필수입니다.")
        @Min(value = 1, message = "정답 번호는 1 이상이어야 합니다.")
        @Max(value = 4, message = "정답 번호는 4 이하이어야 합니다.")
        Integer correctOption,

        @Schema(description = "수정할 해설", example = "해외 입국을 위해 여권은 필수입니다.")
        String explanation
) {
}