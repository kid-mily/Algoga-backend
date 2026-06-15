package com.kidmily.algoga_server.chatbot.application.command;

public record RegisterJudgmentQuestionCommand(Long managerId, String question, String answer) {}