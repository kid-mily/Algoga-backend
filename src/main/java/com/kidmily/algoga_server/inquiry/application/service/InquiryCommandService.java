package com.kidmily.algoga_server.inquiry.application.service;

import com.kidmily.algoga_server.inquiry.application.usecase.InquiryCommandUseCase;
import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import com.kidmily.algoga_server.inquiry.exception.InquiryErrorCode;
import com.kidmily.algoga_server.inquiry.exception.InquiryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryCommandService implements InquiryCommandUseCase {

    private final InquiryRepository inquiryRepository;

    @Override
    @Transactional
    public void createInquiry(Long userId, InquiryCategory category, String title, String content) {
        Inquiry manualInquiry = Inquiry.createPending(userId, category, title, content);
        inquiryRepository.save(manualInquiry);
    }

    @Override
    @Transactional
    public void markAnswerRead(Long userId, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        // 본인 문의가 아니면 존재를 노출하지 않고 NOT_FOUND 로 처리
        if (!inquiry.getUserId().equals(userId)) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND);
        }

        inquiry.markAnswerRead();
        inquiryRepository.save(inquiry);
    }
}