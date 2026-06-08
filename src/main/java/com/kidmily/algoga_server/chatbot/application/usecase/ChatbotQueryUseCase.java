// chatbot/application/usecase/ChatbotQueryUseCase.java (신규)
package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import java.util.List;

public interface ChatbotQueryUseCase {
    List<SuggestedQuestionResponse> getSuggestedQuestions();
}