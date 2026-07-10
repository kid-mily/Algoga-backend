package com.kidmily.algoga_server.booking.domain.model;

/**
 * 패키지 예약 진입 경로.
 * - LOUNGE: 패키지 라운지에서 바로 예약 (완강 불필요, 분할/일시불 선택 가능)
 * - COMPLETION: 단과 강의 완강(100%) 후 마이페이지 모달에서 예약 (완강 필수, 일시불 고정)
 */
public enum BookingSource {
    LOUNGE,
    COMPLETION
}
