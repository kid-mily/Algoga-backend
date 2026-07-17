package com.kidmily.algoga_server.chatbot.application.port.out;

import java.util.List;

/**
 * Python RAG+FC 서버(/chat)의 응답.
 *
 * @param answer         생성된 답변(REJECTED 모드면 거절 문구)
 * @param usedTools      모델이 호출한 도구 이름들(예: search_regulation, get_my_payments)
 * @param mode           NORMAL | REJECTED | AGENT_HANDOFF
 * @param handoffSummary AGENT_HANDOFF 인 경우 LLM 이 만든 대화 요약(상담원용)
 * @param handoffInquiry AGENT_HANDOFF 인 경우 사용자가 입력한 원본 문의내용
 */
public record RagAnswer(String answer, List<String> usedTools, String mode,
                        String handoffSummary, String handoffInquiry) {

    public static final String MODE_NORMAL = "NORMAL";
    public static final String MODE_REJECTED = "REJECTED";
    public static final String MODE_AGENT_HANDOFF = "AGENT_HANDOFF";

    public boolean isRejected() {
        return MODE_REJECTED.equals(mode);
    }

    public boolean isHandoff() {
        return MODE_AGENT_HANDOFF.equals(mode);
    }
}
