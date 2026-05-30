package com.kidmily.algoga_server.refund.domain.event;

public record RefundRejectedEvent(
        Long userId,
        Long referenceId,
        String type  // "LECTURE" 또는 "TRIP"
) {
    public static RefundRejectedEvent ofLecture(Long userId, Long courseId) {
        return new RefundRejectedEvent(userId, courseId, "LECTURE");
    }

    public static RefundRejectedEvent ofTrip(Long userId, Long accommodationId) {
        return new RefundRejectedEvent(userId, accommodationId, "TRIP");
    }
}