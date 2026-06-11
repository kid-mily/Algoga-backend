package com.kidmily.algoga_server.inquiry.application.usecase;

import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.inquiry.application.dto.InquiryAdminDto;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;

public interface InquiryAdminQueryUseCase {
    PageResponse<InquiryAdminDto> getAdminInquiries(InquiryCategory category, int page);
}