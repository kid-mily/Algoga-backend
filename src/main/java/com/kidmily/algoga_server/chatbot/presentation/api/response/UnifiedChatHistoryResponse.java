package com.kidmily.algoga_server.chatbot.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record UnifiedChatHistoryResponse(
        @Schema(description = "메시지 고유 식별자") String id,
        @Schema(description = "메시지 타입 (CHATBOT / INQUIRY)") String type,
        @Schema(description = "사용자 질문 또는 문의 내용") String question,
        @Schema(description = "답변 내용 (대기 상태 시 null 반환)", nullable = true) String answer,
        @Schema(description = "현재 상태 (COMPLETED / FILTERED / PENDING / ANSWERED)") String status,
        @Schema(description = "메시지 생성 일시") Instant createdAt
) {}