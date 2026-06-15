package com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "judgment_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JudgmentQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long judgmentQuestionId;

    @Column(nullable = false)
    private Long managerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public JudgmentQuestionEntity(Long judgmentQuestionId, Long managerId, String question, String answer, Instant createdAt) {
        this.judgmentQuestionId = judgmentQuestionId;
        this.managerId = managerId;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
    }
}