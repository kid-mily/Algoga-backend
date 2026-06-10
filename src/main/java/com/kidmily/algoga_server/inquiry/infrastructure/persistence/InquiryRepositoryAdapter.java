package com.kidmily.algoga_server.inquiry.infrastructure.persistence;

import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import com.kidmily.algoga_server.inquiry.infrastructure.mapper.InquiryMapper;
import com.kidmily.algoga_server.inquiry.infrastructure.persistence.repository.JpaInquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InquiryRepositoryAdapter implements InquiryRepository {

    private final JpaInquiryRepository jpaInquiryRepository;
    private final InquiryMapper inquiryMapper;

    @Override
    public Inquiry save(Inquiry inquiry) {
        return inquiryMapper.toDomain(jpaInquiryRepository.save(inquiryMapper.toJpaEntity(inquiry)));
    }

    @Override
    public int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end) {
        return jpaInquiryRepository.countByUserIdAndCreatedAtBetween(userId, start, end);
    }

    @Override
    public List<Inquiry> findByUserId(Long userId) {
        return jpaInquiryRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(inquiryMapper::toDomain).toList();
    }
}