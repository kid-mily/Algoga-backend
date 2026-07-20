package com.kidmily.algoga_server.inquiry.presentation.api.response;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryStatus;
import java.time.Instant;
public record InquiryAdminResponse(
        Long inquiryId, Long userId, String userName, String userNickname,
        InquiryCategory category, String title, String content,
        String answer, InquiryStatus status, Instant createdAt, Instant answeredAt
) {}