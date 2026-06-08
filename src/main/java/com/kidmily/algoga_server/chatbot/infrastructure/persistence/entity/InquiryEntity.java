package com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity;

import com.kidmily.algoga_server.chatbot.domain.model.InquiryStatus;
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
    private Long managerId; // 관리자가 1:1 문의에 답변을 달 때 업데이트됨 (null 허용)

    @Builder
    public InquiryEntity(Long inquiryId, Long managerId, Long userId, String question, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt) {
        this.inquiryId = inquiryId;
        this.managerId = managerId; // 추가
        this.userId = userId;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.createdAt = createdAt;
        this.answeredAt = answeredAt;
    }
}