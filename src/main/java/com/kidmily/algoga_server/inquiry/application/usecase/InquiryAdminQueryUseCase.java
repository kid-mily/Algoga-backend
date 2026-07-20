package com.kidmily.algoga_server.inquiry.application.usecase;

import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.inquiry.application.dto.InquiryAdminDto;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;

public interface InquiryAdminQueryUseCase {
    // 🌟 status 파라미터 추가
    PageResponse<InquiryAdminDto> getAdminInquiries(InquiryCategory category, InquiryStatus status, int page);

    // 🌟 답변 등록 화면 등에서 사용할 단건 상세 조회 (유저 이름/닉네임 포함)
    InquiryAdminDto getAdminInquiry(Long inquiryId);
}