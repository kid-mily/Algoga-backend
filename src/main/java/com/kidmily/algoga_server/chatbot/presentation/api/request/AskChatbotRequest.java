// chatbot/presentation/api/request/AskChatbotRequest.java
package com.kidmily.algoga_server.chatbot.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size; // 추가

@Schema(description = "챗봇 직접 입력 질문 요청 DTO")
public record AskChatbotRequest(
        @NotBlank(message = "질문 내용은 필수입니다.")
        @Size(max = 500, message = "질문은 최대 500자까지 입력 가능합니다.") // 🌟 글자 수 제한 추가
        @Schema(description = "사용자가 입력한 질문", example = "결제 취소는 어떻게 하나요?")
        String question
) {}