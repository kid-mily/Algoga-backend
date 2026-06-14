package com.kidmily.algoga_server.chatbot.application.command;

public record AskChatbotCommand(Long userId, String question) {}