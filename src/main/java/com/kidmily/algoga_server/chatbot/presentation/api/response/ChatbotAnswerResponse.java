package com.kidmily.algoga_server.chatbot.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatbotAnswerResponse(
        @Schema(description = "챗봇의 답변 내용", example = "결제 취소는 마이페이지에서 가능합니다.")
        String answer,
        @Schema(description = "정상 답변 여부", example = "true")
        boolean isSuccess,
        @Schema(description = "대화 모드. NORMAL(정상) | REJECTED(도메인 외 차단) | AGENT_HANDOFF(상담원 연결 전환) "
                + "| RATE_LIMITED(요청 제한). 프론트는 AGENT_HANDOFF 수신 시 입력 UI 를 상담원 연결 모드로 전환하고, "
                + "handoffSummary·handoffInquiry 로 문의 폼을 미리 채운다.", example = "NORMAL")
        String mode,
        @Schema(description = "상담원 연결(AGENT_HANDOFF) 시 AI 가 만든 대화 요약. 상담원이 맥락을 바로 파악하게 한다.",
                nullable = true)
        String handoffSummary,
        @Schema(description = "상담원 연결(AGENT_HANDOFF) 시 사용자가 입력한 원본 문의내용", nullable = true)
        String handoffInquiry
) {
    public static ChatbotAnswerResponse normal(String answer) {
        return new ChatbotAnswerResponse(answer, true, "NORMAL", null, null);
    }

    public static ChatbotAnswerResponse rejected(String answer) {
        return new ChatbotAnswerResponse(answer, false, "REJECTED", null, null);
    }

    public static ChatbotAnswerResponse agentHandoff(String answer, String handoffSummary, String handoffInquiry) {
        return new ChatbotAnswerResponse(answer, true, "AGENT_HANDOFF", handoffSummary, handoffInquiry);
    }

    public static ChatbotAnswerResponse rateLimited(String answer) {
        return new ChatbotAnswerResponse(answer, false, "RATE_LIMITED", null, null);
    }
}
