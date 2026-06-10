// inquiry/infrastructure/persistence/entity/InquiryEntity.java
package com.kidmily.algoga_server.inquiry.infrastructure.persistence.entity;

import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant answeredAt;

    @Column(name = "manager_id")
    private Long managerId;

    @Builder
    public InquiryEntity(Long inquiryId, Long userId, Long managerId, String question, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt) {
        this.inquiryId = inquiryId;
        this.userId = userId;
        this.managerId = managerId;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.createdAt = createdAt;
        this.answeredAt = answeredAt;
    }
}