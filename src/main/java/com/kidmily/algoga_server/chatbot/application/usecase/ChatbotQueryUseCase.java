// chatbot/application/usecase/ChatbotQueryUseCase.java
package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.UnifiedChatHistoryResponse;
import java.util.List;

public interface ChatbotQueryUseCase {

    // 1. 프론트엔드 버튼용 예상 질문 목록 조회
    List<SuggestedQuestionResponse> getSuggestedQuestions();

    // 2. 챗봇 대화 내용 + 1:1 수동 문의 통합 타임라인 조회
    List<UnifiedChatHistoryResponse> getUnifiedChatHistory(Long userId);
}