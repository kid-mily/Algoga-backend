package com.kidmily.algoga_server.benefit.application.result;

import org.springframework.data.domain.Page;

public record AdminMileageSummaryResult(
        long totalUserCount,
        int totalMileage,
        int totalEarnedMileage,
        int totalUsedMileage,
        Page<AdminMileageUserResult> users
) {
}
