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
    // 답변을 사용자가 확인했는지 여부. 답변 등록 시 false, 사용자가 확인하면 true.
    // null(과거 데이터)은 '확인함'으로 간주해 뱃지를 띄우지 않는다.
    private Boolean answerRead;

    @Builder
    private Inquiry(Long inquiryId, Long userId, Long managerId, InquiryCategory category, String title, String content, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt, Boolean answerRead) {
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
        this.answerRead = answerRead;
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
        this.answerRead = false; // 새 답변은 '미확인' 상태 → 챗봇 창 '답변 완료' 뱃지 표시
    }

    /** 사용자가 답변을 확인했을 때 호출. 뱃지를 해제한다. */
    public void markAnswerRead() {
        this.answerRead = true;
    }

    /** 답변이 등록됐지만 사용자가 아직 확인하지 않았는지 여부(챗봇 '답변 완료' 뱃지 표시용). */
    public boolean isAnswerUnread() {
        return this.status == InquiryStatus.ANSWERED && Boolean.FALSE.equals(this.answerRead);
    }

    public String getQuestion() {
        return "[" + this.category.getDescription() + "] " + this.title + "\n" + this.content;
    }

    public static Inquiry reconstitute(Long inquiryId, Long userId, Long managerId, InquiryCategory category, String title, String content, String answer, InquiryStatus status, Instant createdAt, Instant answeredAt, Boolean answerRead) {
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
                .answerRead(answerRead)
                .build();
    }
}