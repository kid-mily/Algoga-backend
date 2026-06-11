package com.kidmily.algoga_server.stats.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataPaymentAttemptRepository extends JpaRepository<PaymentAttemptJpaEntity, Long> {

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByAccommodationIdAndCreatedAtBetween(Long accommodationId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT DISTINCT e.accommodationId FROM PaymentAttemptJpaEntity e WHERE e.createdAt BETWEEN :from AND :to")
    List<Long> findDistinctAccommodationIdsByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
