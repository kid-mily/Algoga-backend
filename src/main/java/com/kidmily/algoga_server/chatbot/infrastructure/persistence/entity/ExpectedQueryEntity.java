package com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expected_queries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpectedQueryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expectedQueryId;

    @Column(nullable = false)
    private Long knowledgeId;

    @Column(nullable = false, length = 1000)
    private String queryText;

    @Builder
    public ExpectedQueryEntity(Long expectedQueryId, Long knowledgeId, String queryText) {
        this.expectedQueryId = expectedQueryId;
        this.knowledgeId = knowledgeId;
        this.queryText = queryText;
    }
}