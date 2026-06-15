package com.kidmily.algoga_server.inquiry.application.usecase;

import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.inquiry.application.dto.InquiryAdminDto;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;

public interface InquiryAdminQueryUseCase {
    // 🌟 status 파라미터 추가
    PageResponse<InquiryAdminDto> getAdminInquiries(InquiryCategory category, InquiryStatus status, int page);
}