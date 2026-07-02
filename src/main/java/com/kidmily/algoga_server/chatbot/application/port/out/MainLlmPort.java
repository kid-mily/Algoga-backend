package com.kidmily.algoga_server.chatbot.application.port.out;

import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import java.util.List;

public interface MainLlmPort {
    String generateAnswer(String question, String context, List<ChatLog> chatHistory);
}