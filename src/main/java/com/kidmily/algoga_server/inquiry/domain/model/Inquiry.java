// inquiry/domain/model/Inquiry.java
package com.kidmily.algoga_server.inquiry.domain.model;

import com.kidmily.algoga_server.inquiry.exception.InquiryErrorCode;
import com.kidmily.algoga_server.inquiry.exception.InquiryException;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
public class Inquiry {
    private final Long inquiryId;
    private final Long userId;
    private final Long managerId; 
    private final InquiryCategory category;
    private final String title;
    private final String content;
    private String answer;
    private InquiryStatus status;
    private Instant createdAt;
    private Instant answeredAt;

    @Builder
    private Inquiry(Long inquiryId, Long userId, Long managerId, InquiryCategory category, String title, String content, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt) {
        this.inquiryId = inquiryId;
        this.userId = userId;
        this.managerId = managerId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.answer = answer;
        this.status = status;
        this.createdAt = createdAt;
        this.answeredAt = answeredAt;
    }

    public static Inquiry createPending(Long userId, InquiryCategory category, String title, String content) {
        // 🌟 수정됨: BusinessException -> InquiryException 교체
        if (userId == null) throw new InquiryException(InquiryErrorCode.INVALID_USER_ID);
        if (title == null || title.trim().length() < 2) throw new InquiryException(InquiryErrorCode.INVALID_INQUIRY_TITLE);
        if (content == null || content.trim().length() < 5) throw new InquiryException(InquiryErrorCode.INVALID_INQUIRY_CONTENT);

        return Inquiry.builder()
                .userId(userId)
                .category(category)
                .title(title)
                .content(content)
                .status(InquiryStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public void answer(Long managerId, String answer) {
        if (this.status == InquiryStatus.ANSWERED) {
            // 🌟 수정됨: BusinessException -> InquiryException 교체
            throw new InquiryException(InquiryErrorCode.ALREADY_ANSWERED_INQUIRY);
        }
        this.answer = answer;
        this.status = InquiryStatus.ANSWERED;
        this.answeredAt = Instant.now();
    }

    public String getQuestion() {
        return "[" + this.category.getDescription() + "] " + this.title + "\n" + this.content;
    }

    public static Inquiry reconstitute(Long inquiryId, Long userId, Long managerId, InquiryCategory category, String title, String content, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt) {
        return Inquiry.builder()
                .inquiryId(inquiryId)
                .userId(userId)
                .managerId(managerId)
                .category(category)
                .title(title)
                .content(content)
                .answer(answer)
                .status(status)
                .createdAt(createdAt)
                .answeredAt(answeredAt)
                .build();
    }
}