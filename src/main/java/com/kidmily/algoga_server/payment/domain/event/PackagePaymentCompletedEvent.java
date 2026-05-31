package com.kidmily.algoga_server.payment.domain.event;

import java.time.LocalDate;

public record PackagePaymentCompletedEvent(
        Long userId,
        Long accommodationId,
        Long bookingId,
        LocalDate checkInDate
) {}