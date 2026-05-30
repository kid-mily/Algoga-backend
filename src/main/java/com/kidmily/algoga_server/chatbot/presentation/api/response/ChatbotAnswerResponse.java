package com.kidmily.algoga_server.chatbot.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챗봇 답변 응답 DTO")
public record ChatbotAnswerResponse(
        @Schema(description = "챗봇이 생성한 답변 내용", example = "결제 취소를 원하시는군요. 마이페이지 > 결제 내역에서...")
        String answer,

        @Schema(description = "정상 처리 여부 (false일 경우 터무니없는 질문으로 필터링됨)", example = "true")
        boolean isSuccess
) {}