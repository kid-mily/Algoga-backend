package com.kidmily.algoga_server.chatbot.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record UnifiedChatHistoryResponse(
        @Schema(description = "메시지 고유 식별자") String id,
        @Schema(description = "메시지 타입 (CHATBOT / INQUIRY)") String type,
        @Schema(description = "사용자 질문 또는 문의 내용") String question,
        @Schema(description = "답변 내용 (대기 상태 시 null 반환)", nullable = true) String answer,
        @Schema(description = "현재 상태 (COMPLETED / FILTERED / PENDING / ANSWERED)") String status,
        @Schema(description = "메시지 생성 일시") Instant createdAt,
        @Schema(description = "문의 답변이 등록됐지만 사용자가 아직 확인하지 않았는지 여부. "
                + "type=INQUIRY 이고 답변 알림이 미읽음일 때만 true. 챗봇 창에서 '답변 완료' 뱃지 표시에 사용한다. "
                + "챗봇 대화(type=CHATBOT)는 항상 false.", example = "true")
        boolean isAnswerUnread
) {}