package com.kidmily.algoga_server.inquiry.infrastructure.persistence.entity;

import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryId;

    @Column(nullable = false) private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private InquiryCategory category; // 🌟 카테고리 매핑
    
    @Column(nullable = false) private String title;             // 🌟 제목 매핑
    
    @Column(nullable = false, columnDefinition = "TEXT") 
    private String content;                                     // 🌟 내용 매핑

    @Column(columnDefinition = "TEXT") private String answer;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private InquiryStatus status;

    @Column(nullable = false, updatable = false) private Instant createdAt;
    private Instant answeredAt;

    @Column(name = "manager_id") private Long managerId;

    // 답변을 사용자가 확인했는지 여부. nullable → 기존 데이터(과거 답변)는 null 로 남고 도메인에서 '확인함'으로 간주.
    @Column(name = "answer_read") private Boolean answerRead;

    @Builder
    public InquiryEntity(Long inquiryId, Long userId, Long managerId, InquiryCategory category, String title, String content, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt, Boolean answerRead) {
        this.inquiryId = inquiryId; this.userId = userId; this.managerId = managerId;
        this.category = category; this.title = title; this.content = content;
        this.answer = answer; this.status = status; this.createdAt = createdAt; this.answeredAt = answeredAt;
        this.answerRead = answerRead;
    }
}