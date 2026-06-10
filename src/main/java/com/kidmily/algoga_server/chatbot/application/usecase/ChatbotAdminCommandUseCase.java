// chatbot/application/usecase/ChatbotAdminCommandUseCase.java
package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.application.command.RegisterJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateJudgmentQuestionCommand;

public interface ChatbotAdminCommandUseCase {
    void registerJudgmentQuestion(RegisterJudgmentQuestionCommand command);
    void updateJudgmentQuestion(UpdateJudgmentQuestionCommand command); // 🌟 추가됨
    void deleteJudgmentQuestion(Long judgmentQuestionId);               // 🌟 추가됨
}