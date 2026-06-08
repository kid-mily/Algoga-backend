// chatbot/infrastructure/persistence/entity/SuggestedQuestionEntity.java
package com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "suggested_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuggestedQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long suggestedQuestionId;

    @Column(nullable = false)
    private Long managerId; // 예상 질문을 등록한 관리자의 외래키

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public SuggestedQuestionEntity(Long suggestedQuestionId, Long managerId, String question, String answer, Instant createdAt) {
        this.suggestedQuestionId = suggestedQuestionId;
        this.managerId = managerId;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }
}