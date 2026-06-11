package com.kidmily.algoga_server.inquiry.application.service;

import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.inquiry.application.dto.InquiryAdminDto;
import com.kidmily.algoga_server.inquiry.application.usecase.InquiryAdminQueryUseCase;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
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
    public PageResponse<InquiryAdminDto> getAdminInquiries(InquiryCategory category, int page) {
        // 🌟 요구사항: 한 페이지에 8개씩 (최신순 정렬은 어댑터/레포지토리에서 처리)
        Pageable pageable = PageRequest.of(page, 8);
        
        Page<InquiryAdminDto> dtoPage = inquiryRepository.findInquiriesForAdmin(category, pageable)
                .map(inq -> new InquiryAdminDto(
                        inq.getInquiryId(), inq.getUserId(), inq.getCategory(), 
                        inq.getTitle(), inq.getContent(), inq.getAnswer(), 
                        inq.getStatus(), inq.getCreatedAt(), inq.getAnsweredAt()
                ));

        return PageResponse.from(dtoPage);
    }
}