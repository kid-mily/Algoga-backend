// inquiry/domain/model/InquiryStatus.java
package com.kidmily.algoga_server.inquiry.domain.model;

public enum InquiryStatus {
    PENDING,    // 처리 대기 (사용자 1:1 수동 문의)
    COMPLETED,  // 답변 완료 (정상적인 AI 챗봇 답변)
    FILTERED    // 🌟 도메인 외 질문으로 AI 필터링됨
}