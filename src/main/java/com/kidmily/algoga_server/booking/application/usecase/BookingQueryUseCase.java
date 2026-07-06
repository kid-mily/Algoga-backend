package com.kidmily.algoga_server.booking.application.usecase;

import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;

import java.util.List;

public interface BookingQueryUseCase {

    BookingResponse getBooking(Long bookingId);

    List<BookingResponse> getMyBookings(Long userId);

    List<BookingResponse> getMyBookingsByCountry(Long userId, Long countryId);

    boolean hasActiveBooking(Long userId);
}