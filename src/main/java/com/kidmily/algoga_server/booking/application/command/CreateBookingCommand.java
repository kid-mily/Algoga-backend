package com.kidmily.algoga_server.booking.application.command;

import com.kidmily.algoga_server.booking.domain.model.BookingSource;

import java.time.LocalDate;

public record CreateBookingCommand(
        Long accommodationId,
        Long userId,
        String flightInfo,
        String returnFlightInfo,
        String passengerInfo,
        int flightPrice,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BookingSource bookingSource,
        Long packageId,
        Long courseId
) {
}