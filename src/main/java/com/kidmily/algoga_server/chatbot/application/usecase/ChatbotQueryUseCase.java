package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.UnifiedChatHistoryResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import java.util.List;

public interface ChatbotQueryUseCase {
    List<SuggestedQuestionResponse> getSuggestedQuestions();
    PageResponse<UnifiedChatHistoryResponse> getUnifiedChatHistory(Long userId, int page, int size);
}