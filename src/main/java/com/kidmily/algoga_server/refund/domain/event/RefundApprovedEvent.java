package com.kidmily.algoga_server.refund.domain.event;

import java.time.LocalDateTime;

/**
 * 환불 승인 완료 시 발행되는 도메인 이벤트
 * 캘린더 및 알림 도메인 등에서 구독하여 동기화 작업에 활용합니다.
 */
public record RefundApprovedEvent(
        Long userId,
        String userEmail,
        String userName,
        Long referenceId,       // 캘린더 테이블의 referenceId와 매핑되는 ID (courseId 또는 accommodationId)
        Long bookingId,
        String type,             // "LECTURE" (강의) 또는 "TRIP" (여행/패키지)
        String bookingNumber,   // 추가
        int refundAmount,       // 추가
        LocalDateTime refundedAt // 추가
) {
    /**
     * 강의(Lecture) 환불 승인 시 이벤트를 생성하는 팩토리 메서드
     */
    public static RefundApprovedEvent ofLecture(Long userId, String userEmail, String userName,
                                                Long courseId, String bookingNumber,
                                                int refundAmount, LocalDateTime refundedAt) {
        return new RefundApprovedEvent(userId, userEmail, userName, courseId, null,
                "LECTURE", bookingNumber, refundAmount, refundedAt);
    }

    /**
     * 여행/패키지(Trip) 환불 승인 시 이벤트를 생성하는 팩토리 메서드
     */
    public static RefundApprovedEvent ofTrip(Long userId, String userEmail, String userName,
                                             Long accommodationId, Long bookingId, String bookingNumber,
                                             int refundAmount, LocalDateTime refundedAt) {
        return new RefundApprovedEvent(userId, userEmail, userName, accommodationId, bookingId,
                "TRIP", bookingNumber, refundAmount, refundedAt);
    }
}