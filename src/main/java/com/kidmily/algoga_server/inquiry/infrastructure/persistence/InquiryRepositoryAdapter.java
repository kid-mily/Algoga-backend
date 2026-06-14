package com.kidmily.algoga_server.inquiry.infrastructure.persistence;

import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import com.kidmily.algoga_server.inquiry.infrastructure.mapper.InquiryMapper;
import com.kidmily.algoga_server.inquiry.infrastructure.persistence.repository.JpaInquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    public Optional<Inquiry> findById(Long inquiryId) {
        return jpaInquiryRepository.findById(inquiryId).map(inquiryMapper::toDomain);
    }

    @Override
    public List<Inquiry> findByUserId(Long userId) {
        return jpaInquiryRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(inquiryMapper::toDomain).toList();
    }

    @Override
    public int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end) {
        return jpaInquiryRepository.countByUserIdAndCreatedAtBetween(userId, start, end);
    }

    // 🌟 어드민 교차 페이징 조회 동적 분기 구현
    @Override
    public Page<Inquiry> findInquiriesForAdmin(InquiryCategory category, InquiryStatus status, Pageable pageable) {
        // Case 1: 둘 다 조건 없음 (ALL / ALL)
        if (category == null && status == null) {
            return jpaInquiryRepository.findAllByOrderByCreatedAtDesc(pageable).map(inquiryMapper::toDomain);
        }
        
        // Case 2: 카테고리만 조건 지정 (특정 태그 / ALL)
        if (category != null && status == null) {
            return jpaInquiryRepository.findByCategoryOrderByCreatedAtDesc(category, pageable).map(inquiryMapper::toDomain);
        }
        
        // Case 3: 답변 상태만 조건 지정 (ALL / 특정 상태)
        if (category == null && status != null) {
            return jpaInquiryRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(inquiryMapper::toDomain);
        }
        
        // Case 4: 둘 다 조건 지정 (특정 태그 / 특정 상태)
        return jpaInquiryRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, status, pageable).map(inquiryMapper::toDomain);
    }
}