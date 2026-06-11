package com.kidmily.algoga_server.booking.infrastructure.persistence;

import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, Long> {

    List<BookingJpaEntity> findByUserId(Long userId);

    boolean existsByUserIdAndStatusIn(Long userId, List<BookingStatus> statuses);
}