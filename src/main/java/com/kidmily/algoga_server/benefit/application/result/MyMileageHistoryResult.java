package com.kidmily.algoga_server.benefit.application.result;

import java.time.LocalDateTime;

public record MyMileageHistoryResult(
        Long mileageHistoryId,
        Long courseId,
        String courseTitle,
        int amount,
        String type,
        String reason,
        LocalDateTime createdAt
) {
}