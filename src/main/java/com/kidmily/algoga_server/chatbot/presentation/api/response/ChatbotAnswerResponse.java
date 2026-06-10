// chatbot/presentation/api/response/ChatbotAnswerResponse.java
package com.kidmily.algoga_server.chatbot.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챗봇 답변 응답 DTO")
public record ChatbotAnswerResponse(
        @Schema(description = "챗봇의 답변 내용 (필터링 시 거절 안내 메시지)", example = "결제 취소는 마이페이지에서 가능합니다.")
        String answer,

        @Schema(description = "정상 답변 여부 (true: 정상 답변, false: 도메인 외 질문으로 필터링됨)", example = "true")
        boolean isSuccess
) {}