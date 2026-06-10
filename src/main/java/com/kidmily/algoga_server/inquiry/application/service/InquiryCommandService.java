package com.kidmily.algoga_server.inquiry.application.service;

import com.kidmily.algoga_server.inquiry.application.usecase.InquiryCommandUseCase;
import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryCommandService implements InquiryCommandUseCase {

    private final InquiryRepository inquiryRepository;

    @Override
    @Transactional
    public void createInquiry(Long userId, String question) {
        Inquiry manualInquiry = Inquiry.createPending(userId, question);
        inquiryRepository.save(manualInquiry);
    }
}