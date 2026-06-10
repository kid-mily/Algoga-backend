package com.kidmily.algoga_server.inquiry.domain.repository;

import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import java.time.Instant;
import java.util.List;

public interface InquiryRepository {
    Inquiry save(Inquiry inquiry);
    int countByUserIdAndCreatedAtBetween(Long userId, Instant start, Instant end);
    List<Inquiry> findByUserId(Long userId);
}