package com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "knowledges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long knowledgeId;

    @Column(nullable = false)
    private Long managerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public KnowledgeEntity(Long knowledgeId, Long managerId, String content, Instant createdAt) {
        this.knowledgeId = knowledgeId;
        this.managerId = managerId;
        this.content = content;
        this.createdAt = createdAt;
    }
}