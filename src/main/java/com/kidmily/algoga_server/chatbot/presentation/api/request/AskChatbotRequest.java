// chatbot/presentation/api/request/AskChatbotRequest.java
package com.kidmily.algoga_server.chatbot.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskChatbotRequest(
        @NotBlank(message = "질문 내용은 필수입니다.")
        @Size(max = 1000, message = "질문은 1000자 이내여야 합니다.")
        @Schema(description = "사용자가 챗봇에게 직접 입력한 질문", example = "결제 내역은 어디서 확인하나요?")
        String question
) {}