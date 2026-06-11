package com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "chat_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatLogId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private boolean isFiltered;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public ChatLogEntity(Long chatLogId, Long userId, String question, String answer, boolean isFiltered, Instant createdAt) {
        this.chatLogId = chatLogId;
        this.userId = userId;
        this.question = question;
        this.answer = answer;
        this.isFiltered = isFiltered;
        this.createdAt = createdAt;
    }
}