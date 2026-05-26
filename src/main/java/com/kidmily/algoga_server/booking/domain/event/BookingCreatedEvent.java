package com.kidmily.algoga_server.booking.domain.event;

import java.time.LocalDate;

public record BookingCreatedEvent(
        Long userId,
        Long packageId,
        LocalDate departureDate
) {}