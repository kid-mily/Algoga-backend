package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.application.command.RegisterJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.RegisterSuggestedQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateSuggestedQuestionCommand;

public interface ChatbotAdminCommandUseCase {
    void registerJudgmentQuestion(RegisterJudgmentQuestionCommand command);
    void updateJudgmentQuestion(UpdateJudgmentQuestionCommand command);
    void deleteJudgmentQuestion(Long judgmentQuestionId);
    void registerSuggestedQuestion(RegisterSuggestedQuestionCommand command);
    void updateSuggestedQuestion(UpdateSuggestedQuestionCommand command);
    void deleteSuggestedQuestion(Long suggestedQuestionId);
}