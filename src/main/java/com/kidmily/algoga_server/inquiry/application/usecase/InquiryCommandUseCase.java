package com.kidmily.algoga_server.inquiry.application.usecase;

public interface InquiryCommandUseCase {
    void createInquiry(Long userId, String question);
}