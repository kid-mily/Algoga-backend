package com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suggested_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuggestedQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long suggestedQuestionId;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Builder
    public SuggestedQuestionEntity(Long suggestedQuestionId, String question, String answer) {
        this.suggestedQuestionId = suggestedQuestionId;
        this.question = question;
        this.answer = answer;
    }
}