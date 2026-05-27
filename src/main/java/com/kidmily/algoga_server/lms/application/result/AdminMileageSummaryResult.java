package com.kidmily.algoga_server.lms.application.result;

import java.util.List;

public record AdminMileageSummaryResult(
        int totalUserCount,
        int totalMileage,
        int totalEarnedMileage,
        int totalUsedMileage,
        List<AdminMileageUserResult> users
) {
}