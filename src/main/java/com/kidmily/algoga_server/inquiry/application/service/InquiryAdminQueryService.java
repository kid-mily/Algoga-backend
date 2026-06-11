package com.kidmily.algoga_server.inquiry.application.service;

import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.inquiry.application.dto.InquiryAdminDto;
import com.kidmily.algoga_server.inquiry.application.usecase.InquiryAdminQueryUseCase;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryAdminQueryService implements InquiryAdminQueryUseCase {

    private final InquiryRepository inquiryRepository;

    @Override
    public PageResponse<InquiryAdminDto> getAdminInquiries(InquiryCategory category, InquiryStatus status, int page) {
        Pageable pageable = PageRequest.of(page, 8);
        
        // 🌟 도메인 레포지토리에 status 조건 함께 전달
        Page<InquiryAdminDto> dtoPage = inquiryRepository.findInquiriesForAdmin(category, status, pageable)
                .map(inq -> new InquiryAdminDto(
                        inq.getInquiryId(), inq.getUserId(), inq.getCategory(), 
                        inq.getTitle(), inq.getContent(), inq.getAnswer(), 
                        inq.getStatus(), inq.getCreatedAt(), inq.getAnsweredAt()
                ));

        return PageResponse.from(dtoPage);
    }
}