package com.kidmily.algoga_server.inquiry.application.usecase;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;

public interface InquiryCommandUseCase {
    void createInquiry(Long userId, InquiryCategory category, String title, String content);
}