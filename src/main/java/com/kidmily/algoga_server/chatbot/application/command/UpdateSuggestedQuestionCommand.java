package com.kidmily.algoga_server.chatbot.application.command;

public record UpdateSuggestedQuestionCommand(Long suggestedQuestionId, String question, String answer) {}