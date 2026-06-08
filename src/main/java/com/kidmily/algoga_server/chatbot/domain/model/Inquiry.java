// chatbot/domain/model/Inquiry.java
package com.kidmily.algoga_server.chatbot.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
public class Inquiry {
    private Long inquiryId;
    private Long managerId; // 🌟 추가됨: 1:1 문의 답변을 담당할 관리자 ID (외래키 역할)
    private Long userId;
    private String question;
    private String answer;
    private InquiryStatus status;
    private Instant createdAt;
    private Instant answeredAt;

    @Builder
    private Inquiry(Long inquiryId, Long managerId, Long userId, String question, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt) {
        this.inquiryId = inquiryId;
        this.managerId = managerId;
        this.userId = userId;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.createdAt = createdAt;
        this.answeredAt = answeredAt;
    }

    // 🌟 [AI 챗봇 상담 완료 시] 상태를 COMPLETED로 즉시 저장
    public static Inquiry createCompleted(Long userId, String question, String answer) {
        Instant now = Instant.now();
        return Inquiry.builder()
                .managerId(null) // AI가 답변했으므로 매니저 없음
                .userId(userId)
                .question(question)
                .answer(answer)
                .status(InquiryStatus.COMPLETED)
                .createdAt(now)
                .answeredAt(now)
                .build();
    }

    // 🌟 [사용자 직접 문의 시] 상태를 PENDING으로 저장하여 매니저 할당 대기
    public static Inquiry createPending(Long userId, String question) {
        return Inquiry.builder()
                .managerId(null) // 접수 단계이므로 아직 매니저 미할당
                .userId(userId)
                .question(question)
                .answer(null)
                .status(InquiryStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }
}