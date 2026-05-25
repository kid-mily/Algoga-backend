package com.kidmily.algoga_server.booking.application.command;

public record CreateBookingCommand(
        Long packageId,
        Long userId
) {
}