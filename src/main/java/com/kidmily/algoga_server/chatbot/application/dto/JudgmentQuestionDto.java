package com.kidmily.algoga_server.chatbot.application.dto;

import java.time.Instant;

public record JudgmentQuestionDto(
        Long judgmentQuestionId, 
        Long managerId, 
        String question, 
        String answer, 
        Instant createdAt
) {}