package com.kidmily.algoga_server.inquiry.application.usecase;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;

public interface InquiryCommandUseCase {
    void createInquiry(Long userId, InquiryCategory category, String title, String content);

    /** 사용자가 문의 답변을 확인했음을 기록한다. (챗봇 창 '답변 완료' 뱃지 해제용) */
    void markAnswerRead(Long userId, Long inquiryId);
}