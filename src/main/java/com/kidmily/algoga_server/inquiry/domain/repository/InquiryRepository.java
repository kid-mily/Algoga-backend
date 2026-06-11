package com.kidmily.algoga_server.inquiry.domain.repository;

import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;
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
    
    // 🌟 상태 필터링 조회를 지원하도록 포트 아웃풋 확장
    Page<Inquiry> findInquiriesForAdmin(InquiryCategory category, InquiryStatus status, Pageable pageable);
}