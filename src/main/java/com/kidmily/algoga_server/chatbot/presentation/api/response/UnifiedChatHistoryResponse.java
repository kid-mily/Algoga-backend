// chatbot/presentation/api/response/UnifiedChatHistoryResponse.java
package com.kidmily.algoga_server.chatbot.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "통합 채팅방 내역 응답 DTO (챗봇 로그 + 1:1 직접 문의)")
public record UnifiedChatHistoryResponse(
        @Schema(description = "메시지 고유 식별자 (타입 prefix 조합, 예: CHAT_1, INQ_42)", example = "INQ_42")
        String id,

        @Schema(description = "메시지 타입 (CHATBOT / INQUIRY)", example = "INQUIRY")
        String type,

        @Schema(description = "사용자 질문 또는 1:1 문의 내용", example = "결제 취소 절차가 궁금합니다.")
        String question,

        @Schema(description = "답변 내용 (1:1 문의가 대기 상태인 경우 null 반환)", example = "null", nullable = true)
        String answer,

        @Schema(description = "현재 상태 (COMPLETED / FILTERED / PENDING / ANSWERED)", example = "PENDING")
        String status,

        @Schema(description = "메시지 생성 일시")
        Instant createdAt
) {}