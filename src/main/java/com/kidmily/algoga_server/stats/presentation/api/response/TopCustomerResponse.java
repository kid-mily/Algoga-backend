package com.kidmily.algoga_server.stats.presentation.api.response;

public record TopCustomerResponse(
        int rank,
        String userName,
        long bookingCount,
        long totalPaid,
        String recentDestination,
        Double avgIntervalDays
) {}
