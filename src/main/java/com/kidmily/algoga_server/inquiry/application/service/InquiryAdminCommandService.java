// inquiry/application/service/InquiryAdminCommandService.java
package com.kidmily.algoga_server.inquiry.application.service;

import com.kidmily.algoga_server.inquiry.application.usecase.InquiryAdminCommandUseCase;
import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import com.kidmily.algoga_server.inquiry.exception.InquiryErrorCode;
import com.kidmily.algoga_server.inquiry.exception.InquiryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryAdminCommandService implements InquiryAdminCommandUseCase {
    private final InquiryRepository inquiryRepository;

    @Override
    @Transactional
    public void answerInquiry(Long inquiryId, Long managerId, String answer) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                // 🌟 수정됨: BusinessException -> InquiryException 교체
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));
        
        inquiry.answer(managerId, answer);
        inquiryRepository.save(inquiry);
    }
}