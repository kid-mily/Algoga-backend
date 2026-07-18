package com.kidmily.algoga_server.payment.application.command;

import com.kidmily.algoga_server.payment.domain.model.PaymentType;

import java.util.List;

/**
 * 패키지+강의 통합 결제 커맨드.
 * PortOne 결제 1회({@code portonePaymentId})로 예약 결제 1건 + 강의 결제 N건을 함께 기록한다.
 */
public record CreateBundlePaymentCommand(
        Long bookingId,
        List<Long> courseIds,
        Long userId,
        PaymentType paymentType,
        int amount,
        int usedMileage,
        Long usedCouponId,
        String portonePaymentId
) {}
