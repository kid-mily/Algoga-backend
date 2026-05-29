package com.kidmily.algoga_server.booking.application.usecase;

import com.kidmily.algoga_server.booking.application.command.CreateBookingCommand;

public interface BookingCommandUseCase {

    Long handle(CreateBookingCommand command);

    void cancel(Long bookingId, Long userId);
}