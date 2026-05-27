package com.kidmily.algoga_server.benefit.application.result;

import java.time.LocalDateTime;

public record AdminMileageHistoryResult(
        Long mileageHistoryId,
        Long userId,
        String userName,
        String userEmail,
        Long courseId,
        String courseTitle,
        Long managerId,
        String processorName,
        int amount,
        int signedAmount,
        String type,
        String reason,
        LocalDateTime createdAt
) {
}