// chatbot/application/usecase/ChatbotQueryUseCase.java
package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.UnifiedChatHistoryResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse; // 🌟 추가

import java.util.List;

public interface ChatbotQueryUseCase {
    List<SuggestedQuestionResponse> getSuggestedQuestions();
    
    // 🌟 수정됨: 반환형을 PageResponse로 변경하고 페이징 파라미터 추가
    PageResponse<UnifiedChatHistoryResponse> getUnifiedChatHistory(Long userId, int page, int size);
}