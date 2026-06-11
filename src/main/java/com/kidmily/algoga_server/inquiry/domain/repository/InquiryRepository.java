package com.kidmily.algoga_server.inquiry.domain.repository;

import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InquiryRepository {
    Inquiry save(Inquiry inquiry);
    Optional<Inquiry> findById(Long inquiryId);
    List<Inquiry> findByUserId(Long userId);
    int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end);
    
    // 🌟 관리자용: 카테고리 필터링 및 페이징 조회 포트 추가
    Page<Inquiry> findInquiriesForAdmin(InquiryCategory category, Pageable pageable);
}