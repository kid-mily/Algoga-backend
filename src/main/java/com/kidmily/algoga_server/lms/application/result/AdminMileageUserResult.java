package com.kidmily.algoga_server.lms.application.result;

import java.time.LocalDateTime;

public record AdminMileageUserResult(
        Long userId,
        String name,
        String email,
        int totalMileage,
        int totalEarnedMileage,
        int totalUsedMileage,
        LocalDateTime lastUpdatedAt
) {
}