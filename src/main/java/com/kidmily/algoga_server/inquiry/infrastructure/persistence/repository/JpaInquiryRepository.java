package com.kidmily.algoga_server.inquiry.infrastructure.persistence.repository;

import com.kidmily.algoga_server.inquiry.infrastructure.persistence.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface JpaInquiryRepository extends JpaRepository<InquiryEntity, Long> {
    int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end);
    List<InquiryEntity> findByUserIdOrderByCreatedAtAsc(Long userId);
}