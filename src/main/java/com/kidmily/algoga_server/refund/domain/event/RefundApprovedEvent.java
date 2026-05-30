package com.kidmily.algoga_server.refund.domain.event;

/**
 * 환불 승인 완료 시 발행되는 도메인 이벤트
 * 캘린더 및 알림 도메인 등에서 구독하여 동기화 작업에 활용합니다.
 */
public record RefundApprovedEvent(
        Long userId,
        Long referenceId,       // 캘린더 테이블의 referenceId와 매핑되는 ID (courseId 또는 accommodationId)
        String type             // "LECTURE" (강의) 또는 "TRIP" (여행/패키지)
) {
    /**
     * 강의(Lecture) 환불 승인 시 이벤트를 생성하는 팩토리 메서드
     */
    public static RefundApprovedEvent ofLecture(Long userId, Long courseId) {
        return new RefundApprovedEvent(userId, courseId, "LECTURE");
    }

    /**
     * 여행/패키지(Trip) 환불 승인 시 이벤트를 생성하는 팩토리 메서드
     */
    public static RefundApprovedEvent ofTrip(Long userId, Long accommodationId) {
        return new RefundApprovedEvent(userId, accommodationId, "TRIP");
    }
}