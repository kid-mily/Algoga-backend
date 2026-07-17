package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.application.command.RegisterSuggestedQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateSuggestedQuestionCommand;

public interface ChatbotAdminCommandUseCase {
    void registerSuggestedQuestion(RegisterSuggestedQuestionCommand command);
    void updateSuggestedQuestion(UpdateSuggestedQuestionCommand command);
    void deleteSuggestedQuestion(Long suggestedQuestionId);
}