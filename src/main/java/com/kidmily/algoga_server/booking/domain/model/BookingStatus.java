package com.kidmily.algoga_server.booking.domain.model;

public enum BookingStatus {
    PENDING,           // 예약 생성 (결제 전)
    DEPOSIT_PAID,      // 예약금 결제 완료
    FULL_PAID,         // 잔금 결제 완료
    CANCEL_REQUESTED,  // 취소 요청
    REFUNDED,          // 환불 완료
    EXPIRED            // 미결제 상태로 출발일이 지나 만료됨 (배치가 전환)
}