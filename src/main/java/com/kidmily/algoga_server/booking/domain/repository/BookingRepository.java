package com.kidmily.algoga_server.booking.domain.repository;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(Long bookingId);

    List<Booking> findByUserId(Long userId);

    Booking updateStatus(Long bookingId, BookingStatus status);
}