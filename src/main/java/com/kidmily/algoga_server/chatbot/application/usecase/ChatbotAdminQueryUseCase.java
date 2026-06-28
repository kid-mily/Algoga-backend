package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.application.dto.JudgmentQuestionDto;
import com.kidmily.algoga_server.chatbot.application.dto.SuggestedQuestionDto;
import java.util.List;

public interface ChatbotAdminQueryUseCase {
    List<SuggestedQuestionDto> getAllSuggestedQuestions();
    SuggestedQuestionDto getSuggestedQuestion(Long suggestedQuestionId);
}