package com.kidmily.algoga_server.booking.infrastructure.persistence;

import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, Long> {

    List<BookingJpaEntity> findByUserId(Long userId);

    // 출발일(checkInDate)이 기준일보다 이전인 특정 상태 예약 (미결제 만료 배치용)
    List<BookingJpaEntity> findByStatusAndCheckInDateBefore(BookingStatus status, LocalDate date);

    boolean existsByUserIdAndStatusIn(Long userId, List<BookingStatus> statuses);

    long countByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);

    long countByAccommodationIdAndStatusAndCreatedAtBetween(Long accommodationId, BookingStatus status, LocalDateTime from, LocalDateTime to);

    @Query("SELECT DISTINCT e.accommodationId FROM BookingJpaEntity e WHERE e.status = :status AND e.createdAt BETWEEN :from AND :to")
    List<Long> findDistinctAccommodationIdsByStatusAndCreatedAtBetween(@Param("status") BookingStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<BookingJpaEntity> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}