package com.kidmily.algoga_server.booking.domain.repository;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(Long bookingId);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatusAndCheckInDateBefore(BookingStatus status, LocalDate date);

    Booking updateStatus(Long bookingId, BookingStatus status);

    boolean existsByUserIdAndStatusIn(Long userId, List<BookingStatus> statuses);

    long countByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    long countByAccommodationIdAndStatusAndCreatedAtBetween(Long accommodationId, BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Long> findDistinctAccommodationIdsByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Booking> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}