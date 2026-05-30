// chatbot/domain/repository/InquiryRepository.java
package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.Inquiry;
import java.time.Instant; // 추가

public interface InquiryRepository {
    Inquiry save(Inquiry inquiry);
    int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end); // 🌟 오늘 상담 횟수 조회를 위한 메서드 추가
}