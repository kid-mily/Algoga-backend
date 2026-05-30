// chatbot/application/usecase/ChatbotCommandUseCase.java (수정)
package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;

public interface ChatbotCommandUseCase {
    ChatbotAnswerResponse askToChatbot(AskChatbotCommand command);
    ChatbotAnswerResponse askSuggestedQuestion(Long suggestedQuestionId); // 추가
    void createManualInquiry(Long userId, String question); // 추가
}