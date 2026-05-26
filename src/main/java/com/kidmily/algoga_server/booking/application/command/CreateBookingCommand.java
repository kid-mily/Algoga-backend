package com.kidmily.algoga_server.booking.application.command;

import java.time.LocalDate;

public record CreateBookingCommand(
        Long accommodationId,
        Long userId,
        String flightInfo,
        int flightPrice,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}