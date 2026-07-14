package com.kidmily.algoga_server.payment.infrastructure.persistence;

import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {
    Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * 멱등키로 결제 행을 즉시(벌크 SQL) 삭제한다.
     * FAILED 재시도 시 기존 행을 지우고 새로 INSERT 하는데, 파생 delete(엔티티 로드 후 remove)는
     * Hibernate 액션 큐에서 INSERT가 DELETE보다 먼저 flush 되어 UNIQUE 충돌이 날 수 있다.
     * @Modifying 벌크 delete 는 즉시 SQL 로 실행되므로 이후 INSERT 와 순서 충돌이 없다.
     */
    @Modifying
    @Query("delete from PaymentJpaEntity p where p.idempotencyKey = :idempotencyKey")
    void deleteByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
    Optional<PaymentJpaEntity> findByPortonePaymentId(String portonePaymentId);
    List<PaymentJpaEntity> findByUserId(Long userId);
    List<PaymentJpaEntity> findByBookingId(Long bookingId);
    List<PaymentJpaEntity> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<PaymentJpaEntity> findByUserIdAndPaymentTypeAndStatusAndCourseIdIsNotNull(
            Long userId,
            PaymentType paymentType,
            PaymentStatus status
    );

    long countByCourseIdAndPaymentTypeAndStatus(
            Long courseId,
            PaymentType paymentType,
            PaymentStatus status
    );

    List<PaymentJpaEntity> findByBookingIdInAndStatus(List<Long> bookingIds, PaymentStatus status);
}