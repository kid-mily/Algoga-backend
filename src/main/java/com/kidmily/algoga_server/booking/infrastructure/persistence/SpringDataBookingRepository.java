package com.kidmily.algoga_server.booking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBookingRepository extends JpaRepository<com.kidmily.algoga_server.booking.infrastructure.persistence.BookingJpaEntity, Long> {
}