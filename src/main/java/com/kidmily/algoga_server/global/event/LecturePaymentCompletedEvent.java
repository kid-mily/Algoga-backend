package com.kidmily.algoga_server.global.event;

import java.time.LocalDateTime;

public record LecturePaymentCompletedEvent(
        Long userId,
        Long courseId,
        LocalDateTime paidAt
) {
}
