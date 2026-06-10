// chatbot/presentation/api/request/RegisterJudgmentQuestionRequest.java
package com.kidmily.algoga_server.chatbot.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterJudgmentQuestionRequest(
        @NotBlank(message = "판단용 예상 질문은 필수입니다.")
        @Size(max = 1000, message = "질문은 1000자 이내여야 합니다.")
        @Schema(description = "벡터 필터 통과 후 LLM 매칭을 위한 예상 질문", example = "결제 취소는 어떻게 하나요?")
        String question,

        @NotBlank(message = "판단용 예상 답변은 필수입니다.")
        @Size(max = 2000, message = "답변은 2000자 이내여야 합니다.")
        @Schema(description = "매칭 시 LLM에게 제공될 컨텍스트(가이드 답변)", example = "결제 취소는 마이페이지 > 결제 내역에서 결제일로부터 7일 이내에 가능합니다.")
        String answer
) {}