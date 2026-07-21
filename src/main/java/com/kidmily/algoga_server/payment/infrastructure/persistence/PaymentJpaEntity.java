package com.kidmily.algoga_server.payment.infrastructure.persistence;

import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payments_idempotency_key",
                columnNames = "idempotency_key"
        )
)
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
    private Integer usedMileage;

    @Column(name = "used_coupon_id")
    private Long usedCouponId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "portone_payment_id")
    private String portonePaymentId;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PaymentJpaEntity(Long bookingId, Long courseId, Long userId, PaymentType paymentType,
                            int amount, Integer usedMileage, Long usedCouponId,
                            PaymentStatus status, String idempotencyKey,
                            String portonePaymentId, String paymentMethod, String userName,
                            LocalDateTime createdAt) {
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
        this.paymentMethod = paymentMethod;
        this.userName = userName;
        this.createdAt = createdAt;
    }

    /**
     * id 를 포함해 복원하는 생성자.
     * 기존(무 id) 생성자만 있으면 이미 저장된 결제를 save 할 때도 id 가 없어
     * JPA 가 UPDATE 가 아니라 INSERT 를 시도 → idempotency_key UNIQUE 충돌로 실패한다.
     * (예: 환불 완료 시 payment.markRefunded() 후 save 하면 500)
     */
    public PaymentJpaEntity(Long id, Long bookingId, Long courseId, Long userId, PaymentType paymentType,
                            int amount, Integer usedMileage, Long usedCouponId,
                            PaymentStatus status, String idempotencyKey,
                            String portonePaymentId, String paymentMethod, String userName,
                            LocalDateTime createdAt) {
        this(bookingId, courseId, userId, paymentType, amount, usedMileage, usedCouponId,
                status, idempotencyKey, portonePaymentId, paymentMethod, userName, createdAt);
        this.id = id;
    }

    public void updateStatus(PaymentStatus status, String portonePaymentId) {
        this.status = status;
        this.portonePaymentId = portonePaymentId;
    }
}