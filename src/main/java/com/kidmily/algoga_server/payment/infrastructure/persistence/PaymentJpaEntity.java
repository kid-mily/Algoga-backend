package com.kidmily.algoga_server.payment.infrastructure.persistence;

import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "booking_id", nullable = true)
    private Long bookingId;

    @Column(name = "course_id", nullable = true)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "used_mileage")
    private int usedMileage;

    @Column(name = "used_coupon_id")
    private Long usedCouponId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "portone_payment_id")
    private String portonePaymentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PaymentJpaEntity(Long bookingId, Long courseId, Long userId, PaymentType paymentType,
                            int amount, int usedMileage, Long usedCouponId,
                            PaymentStatus status, String idempotencyKey,
                            String portonePaymentId, LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.courseId = courseId;
        this.userId = userId;
        this.paymentType = paymentType;
        this.amount = amount;
        this.usedMileage = usedMileage;
        this.usedCouponId = usedCouponId;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.portonePaymentId = portonePaymentId;
        this.createdAt = createdAt;
    }

    public void updateStatus(PaymentStatus status, String portonePaymentId) {
        this.status = status;
        this.portonePaymentId = portonePaymentId;
    }
}