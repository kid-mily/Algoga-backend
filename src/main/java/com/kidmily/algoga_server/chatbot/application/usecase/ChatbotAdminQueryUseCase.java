// chatbot/application/usecase/ChatbotAdminQueryUseCase.java
package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.presentation.api.response.JudgmentQuestionResponse;
import java.util.List;

public interface ChatbotAdminQueryUseCase {
    List<JudgmentQuestionResponse> getAllJudgmentQuestions();
    JudgmentQuestionResponse getJudgmentQuestion(Long judgmentQuestionId);
}