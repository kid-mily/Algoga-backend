package com.kidmily.algoga_server.inquiry.application.dto;

import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;
import java.time.Instant;

// 애플리케이션 계층 DTO
public record InquiryAdminDto(
        Long inquiryId, Long userId, InquiryCategory category, String title, String content, 
        String answer, InquiryStatus status, Instant createdAt, Instant answeredAt
) {}