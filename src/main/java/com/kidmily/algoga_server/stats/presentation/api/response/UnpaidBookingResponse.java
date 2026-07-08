package com.kidmily.algoga_server.stats.presentation.api.response;

import java.time.LocalDate;

public record UnpaidBookingResponse(
        String bookingNumber,
        String userName,
        String productName,
        long balanceAmount,
        LocalDate depositPaidDate,
        long daysElapsed,
        LocalDate checkInDate,
        Long dday
) {}
