package com.kidmily.algoga_server.chatbot.domain.repository;

public record AskChatbotCommand(Long userId, String question) {}