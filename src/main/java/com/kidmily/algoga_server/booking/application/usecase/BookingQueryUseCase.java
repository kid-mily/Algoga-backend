package com.kidmily.algoga_server.booking.application.usecase;

import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;

public interface BookingQueryUseCase {

    BookingResponse getBooking(Long bookingId);
}