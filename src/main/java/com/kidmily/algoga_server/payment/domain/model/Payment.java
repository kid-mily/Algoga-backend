package com.kidmily.algoga_server.payment.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    private Long id;
    private Long bookingId;
    private Long courseId;
    private Long userId;
    private PaymentType paymentType;
    private int amount;
    private int usedMileage;
    private Long usedCouponId;
    private PaymentStatus status;
    private String idempotencyKey;
    private String portonePaymentId;
    /** 실제 결제수단 (PortOne 응답의 method에서 추출 — 예: TOSSPAY, KAKAOPAY, PaymentMethodCard). null 가능. */
    private String paymentMethod;
    private LocalDateTime createdAt;

    public static Payment create(Long bookingId, Long courseId, Long userId, PaymentType paymentType,
                                 int amount, int usedMileage, Long usedCouponId,
                                 String idempotencyKey, String paymentMethod) {
        Payment payment = new Payment();
        payment.bookingId = bookingId;
        payment.courseId = courseId;
        payment.userId = userId;
        payment.paymentType = paymentType;
        payment.amount = amount;
        payment.usedMileage = usedMileage;
        payment.usedCouponId = usedCouponId;
        payment.status = PaymentStatus.FAILED;
        payment.idempotencyKey = idempotencyKey;
        payment.paymentMethod = paymentMethod;
        payment.createdAt = LocalDateTime.now();
        return payment;
    }

    public static Payment reconstitute(Long id, Long bookingId, Long courseId, Long userId,
                                       PaymentType paymentType, int amount,
                                       int usedMileage, Long usedCouponId,
                                       PaymentStatus status, String idempotencyKey,
                                       String portonePaymentId, String paymentMethod,
                                       LocalDateTime createdAt) {
        Payment payment = new Payment();
        payment.id = id;
        payment.bookingId = bookingId;
        payment.courseId = courseId;
        payment.userId = userId;
        payment.paymentType = paymentType;
        payment.amount = amount;
        payment.usedMileage = usedMileage;
        payment.usedCouponId = usedCouponId;
        payment.status = status;
        payment.idempotencyKey = idempotencyKey;
        payment.portonePaymentId = portonePaymentId;
        payment.paymentMethod = paymentMethod;
        payment.createdAt = createdAt;
        return payment;
    }

    public void markSuccess(String portonePaymentId) {
        this.status = PaymentStatus.SUCCESS;
        this.portonePaymentId = portonePaymentId;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    /** 웹훅 등으로 뒤늦게 결제수단이 확인된 경우 갱신 */
    public void updatePaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}