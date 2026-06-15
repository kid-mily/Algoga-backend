// chatbot/presentation/api/response/JudgmentQuestionResponse.java (DTO)
package com.kidmily.algoga_server.chatbot.application.service;
import java.time.Instant;
public record JudgmentQuestionResponse(Long judgmentQuestionId, Long managerId, String question, String answer, Instant createdAt) {}