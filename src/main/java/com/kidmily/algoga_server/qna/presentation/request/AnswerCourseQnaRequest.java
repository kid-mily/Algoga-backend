package com.kidmily.algoga_server.qna.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "강의 Q&A 답변 등록 요청")
public record AnswerCourseQnaRequest(

        @Schema(description = "답변 내용", example = "오사카 시내 위주 일정이라면 오사카 주유패스를 추천드립니다.")
        @NotBlank(message = "답변 내용은 필수입니다.")
        @Size(max = 2000, message = "답변 내용은 2000자 이하로 입력해주세요.")
        String answer
) {
}
