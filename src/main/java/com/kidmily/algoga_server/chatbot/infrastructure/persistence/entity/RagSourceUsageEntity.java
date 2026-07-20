package com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "rag_source_usages", indexes = {
        @Index(name = "idx_rag_source_usage_created_at", columnList = "createdAt"),
        @Index(name = "idx_rag_source_usage_source", columnList = "source")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagSourceUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ragSourceUsageId;

    // 어떤 대화 로그에서 채택됐는지(드릴다운용). 로그 저장 실패 등으로 없을 수 있어 nullable.
    @Column(name = "chat_log_id")
    private Long chatLogId;

    @Column(nullable = false)
    private String source;

    private Integer page;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public RagSourceUsageEntity(Long ragSourceUsageId, Long chatLogId, String source, Integer page, Instant createdAt) {
        this.ragSourceUsageId = ragSourceUsageId;
        this.chatLogId = chatLogId;
        this.source = source;
        this.page = page;
        this.createdAt = createdAt;
    }
}
