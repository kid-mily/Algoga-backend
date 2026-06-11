package com.kidmily.algoga_server.refund.domain.model;

public enum RefundStatus {
    REQUESTED,    // 환불 요청됨
    UNDER_REVIEW, // CS매니저 → 정산매니저 검토 요청 상태
    APPROVED,     // 승인됨
    REJECTED,     // 반려됨
    COMPLETED     // 완료됨
}