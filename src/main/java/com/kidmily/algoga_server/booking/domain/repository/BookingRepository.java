package com.kidmily.algoga_server.booking.domain.repository;

import com.kidmily.algoga_server.booking.domain.model.Booking;

import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(Long bookingId);

    Booking cancel(Long bookingId);
}