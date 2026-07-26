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

    // 여러 유저의 예약을 한 번에 조회 (통계에서 유저별 findByUserId 반복 호출 = N+1 제거용)
    List<Booking> findByUserIdIn(List<Long> userIds);

    List<Booking> findByStatusAndCheckInDateBefore(BookingStatus status, LocalDate date);

    Booking updateStatus(Long bookingId, BookingStatus status);

    boolean existsByUserIdAndStatusIn(Long userId, List<BookingStatus> statuses);

    long countByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    long countByAccommodationIdAndStatusAndCreatedAtBetween(Long accommodationId, BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Long> findDistinctAccommodationIdsByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    List<Booking> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}