package com.kidmily.algoga_server.inquiry.application.usecase;

public interface InquiryAdminCommandUseCase {
    void answerInquiry(Long inquiryId, Long managerId, String answer);
}