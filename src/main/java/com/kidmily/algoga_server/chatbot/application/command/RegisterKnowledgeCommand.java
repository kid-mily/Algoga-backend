package com.kidmily.algoga_server.chatbot.application.command;

import java.util.List;

public record RegisterKnowledgeCommand(
        Long managerId, 
        String content, 
        List<String> expectedQueries
) {}