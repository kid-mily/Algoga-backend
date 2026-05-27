package com.kidmily.algoga_server.booking.domain.event;

public record BookingCanceledEvent(
        Long accommodationId
) {}