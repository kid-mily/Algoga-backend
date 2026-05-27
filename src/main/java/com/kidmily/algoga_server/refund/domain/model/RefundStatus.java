package com.kidmily.algoga_server.refund.domain.model;

public enum RefundStatus {
    REQUESTED,   // 환불 요청됨
    APPROVED,    // 승인됨
    REJECTED,    // 반려됨
    COMPLETED    // 완료됨
}