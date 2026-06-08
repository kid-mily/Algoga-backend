// chatbot/infrastructure/persistence/InquiryRepositoryAdapter.java
package com.kidmily.algoga_server.chatbot.infrastructure.persistence;

import com.kidmily.algoga_server.chatbot.domain.model.Inquiry;
import com.kidmily.algoga_server.chatbot.domain.repository.InquiryRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.InquiryEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaInquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant; // 추가

@Component
@RequiredArgsConstructor
public class InquiryRepositoryAdapter implements InquiryRepository {

    private final JpaInquiryRepository jpaInquiryRepository;

    @Override
    public Inquiry save(Inquiry inquiry) {
        InquiryEntity entity = InquiryEntity.builder()
                .userId(inquiry.getUserId())
                .question(inquiry.getQuestion())
                .answer(inquiry.getAnswer())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .answeredAt(inquiry.getAnsweredAt())
                .build();

        InquiryEntity savedEntity = jpaInquiryRepository.save(entity);

        return Inquiry.builder()
                .inquiryId(savedEntity.getInquiryId())
                .userId(savedEntity.getUserId())
                .question(savedEntity.getQuestion())
                .answer(savedEntity.getAnswer())
                .status(savedEntity.getStatus())
                .createdAt(savedEntity.getCreatedAt())
                .answeredAt(savedEntity.getAnsweredAt())
                .build();
    }

    // 🌟 횟수 조회 구현부 추가
    @Override
    public int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end) {
        return jpaInquiryRepository.countByUserIdAndCreatedAtBetween(userId, start, end);
    }
}