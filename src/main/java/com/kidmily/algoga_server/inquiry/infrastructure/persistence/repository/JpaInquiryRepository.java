package com.kidmily.algoga_server.inquiry.infrastructure.persistence.repository;

import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.infrastructure.persistence.entity.InquiryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface JpaInquiryRepository extends JpaRepository<InquiryEntity, Long> {
    int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end);
    List<InquiryEntity> findByUserIdOrderByCreatedAtAsc(Long userId);

    // 🌟 카테고리 유무에 따른 동적 페이징 조회 (시간 최신순)
    Page<InquiryEntity> findByCategoryOrderByCreatedAtDesc(InquiryCategory category, Pageable pageable);
    Page<InquiryEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}