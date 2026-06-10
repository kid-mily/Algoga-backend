package com.kidmily.algoga_server.inquiry.domain.model;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.inquiry.exception.InquiryErrorCode;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
public class Inquiry {
    private Long inquiryId;
    private Long userId;
    private Long managerId;
    private String question;
    private String answer;
    private InquiryStatus status;
    private Instant createdAt;
    private Instant answeredAt;

    @Builder
    private Inquiry(Long inquiryId, Long userId, Long managerId, String question, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt) {
        this.inquiryId = inquiryId;
        this.userId = userId;
        this.managerId = managerId;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.createdAt = createdAt;
        this.answeredAt = answeredAt;
    }

    public static Inquiry createPending(Long userId, String question) {
        if (question == null || question.trim().length() < 2) {
            throw new BusinessException(InquiryErrorCode.INVALID_INQUIRY_QUESTION);
        }
        return Inquiry.builder()
                .userId(userId)
                .question(question)
                .status(InquiryStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public static Inquiry reconstitute(Long inquiryId, Long userId, Long managerId, String question, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt) {
        return Inquiry.builder()
                .inquiryId(inquiryId)
                .userId(userId)
                .managerId(managerId)
                .question(question)
                .answer(answer)
                .status(status)
                .createdAt(createdAt)
                .answeredAt(answeredAt)
                .build();
    }
}