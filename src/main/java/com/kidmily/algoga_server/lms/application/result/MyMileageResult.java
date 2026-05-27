package com.kidmily.algoga_server.lms.application.result;

import java.util.List;

public record MyMileageResult(
        int totalMileage,
        int totalEarnedMileage,
        int totalUsedMileage,
        List<MyMileageHistoryResult> histories
) {
}