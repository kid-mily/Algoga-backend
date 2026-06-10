package com.kidmily.algoga_server.chatbot.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
public class SuggestedQuestion {
    private Long suggestedQuestionId;
    private Long managerId;
    private String question;
    private String answer;
    private Instant createdAt;

    @Builder
    public SuggestedQuestion(Long suggestedQuestionId, Long managerId, String question, String answer, Instant createdAt) {
        this.suggestedQuestionId = suggestedQuestionId;
        this.managerId = managerId;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
    }
}