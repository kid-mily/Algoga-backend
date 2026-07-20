package com.kidmily.algoga_server.inquiry.application.service;

import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.inquiry.application.dto.InquiryAdminDto;
import com.kidmily.algoga_server.inquiry.application.port.UserProfilePort;
import com.kidmily.algoga_server.inquiry.application.usecase.InquiryAdminQueryUseCase;
import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import com.kidmily.algoga_server.inquiry.exception.InquiryErrorCode;
import com.kidmily.algoga_server.inquiry.exception.InquiryException;
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
    private final UserProfilePort userProfilePort;

    @Override
    public PageResponse<InquiryAdminDto> getAdminInquiries(InquiryCategory category, InquiryStatus status, int page) {
        Pageable pageable = PageRequest.of(page, 8);

        // 🌟 도메인 레포지토리에 status 조건 함께 전달 + 유저 이름/닉네임을 포트로 조회해 함께 반환
        Page<InquiryAdminDto> dtoPage = inquiryRepository.findInquiriesForAdmin(category, status, pageable)
                .map(this::toAdminDto);

        return PageResponse.from(dtoPage);
    }

    @Override
    public InquiryAdminDto getAdminInquiry(Long inquiryId) {
        // 🌟 문의 ID 단건 조회. 답변 등록 화면에서 해당 문의 상세를 불러올 때 사용.
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));
        return toAdminDto(inquiry);
    }

    // 도메인 Inquiry + 유저 이름/닉네임을 합쳐 어드민 DTO 로 변환. (목록/단건 조회 공용)
    private InquiryAdminDto toAdminDto(Inquiry inq) {
        // 탈퇴/삭제 등으로 유저를 못 찾으면 이름/닉네임은 null 로 둔다.
        var profile = userProfilePort.findProfile(inq.getUserId()).orElse(null);
        return new InquiryAdminDto(
                inq.getInquiryId(), inq.getUserId(),
                profile != null ? profile.name() : null,
                profile != null ? profile.nickname() : null,
                inq.getCategory(),
                inq.getTitle(), inq.getContent(), inq.getAnswer(),
                inq.getStatus(), inq.getCreatedAt(), inq.getAnsweredAt()
        );
    }
}