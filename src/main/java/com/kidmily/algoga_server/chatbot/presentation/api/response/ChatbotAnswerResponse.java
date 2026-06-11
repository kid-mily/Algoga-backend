package com.kidmily.algoga_server.chatbot.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatbotAnswerResponse(
        @Schema(description = "챗봇의 답변 내용", example = "결제 취소는 마이페이지에서 가능합니다.")
        String answer,
        @Schema(description = "정상 답변 여부", example = "true")
        boolean isSuccess
) {}