// chatbot/infrastructure/persistence/repository/JpaInquiryRepository.java
package com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant; // 추가

public interface JpaInquiryRepository extends JpaRepository<InquiryEntity, Long> {
    int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end); // 🌟 쿼리 메서드 추가
}