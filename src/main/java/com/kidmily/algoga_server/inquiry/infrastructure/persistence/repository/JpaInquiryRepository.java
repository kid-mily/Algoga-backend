package com.kidmily.algoga_server.inquiry.infrastructure.persistence.repository;

import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;
import com.kidmily.algoga_server.inquiry.infrastructure.persistence.entity.InquiryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface JpaInquiryRepository extends JpaRepository<InquiryEntity, Long> {
    int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end);
    List<InquiryEntity> findByUserIdOrderByCreatedAtAsc(Long userId);

    Page<InquiryEntity> findByCategoryOrderByCreatedAtDesc(InquiryCategory category, Pageable pageable);
    Page<InquiryEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 🌟 신규 추가: 상태 단독 조건 페이징 조회
    Page<InquiryEntity> findByStatusOrderByCreatedAtDesc(InquiryStatus status, Pageable pageable);

    // 🌟 신규 추가: 카테고리 및 상태 조합 조건 페이징 조회
    Page<InquiryEntity> findByCategoryAndStatusOrderByCreatedAtDesc(InquiryCategory category, InquiryStatus status, Pageable pageable);
}