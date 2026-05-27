package com.kidmily.algoga_server.payment.infrastructure.mapper;

import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.infrastructure.persistence.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentJpaEntity toJpaEntity(Payment payment){
        return new PaymentJpaEntity(
                payment.getBookingId(),
                payment.getUserId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getUsedMileage(),
                payment.getUsedCouponId(),
                payment.getStatus(),
                payment.getIdempotencyKey(),
                payment.getPortonePaymentId(),
                payment.getCreatedAt()
        );
    }
    public Payment toDomain(PaymentJpaEntity entity) {
        return  Payment.reconstitute(
                entity.getId(),
                entity.getBookingId(),
                entity.getUserId(),
                entity.getPaymentType(),
                entity.getAmount(),
                entity.getUsedMileage(),
                entity.getUsedCouponId(),
                entity.getStatus(),
                entity.getIdempotencyKey(),
                entity.getPortonePaymentId(),
                entity.getCreatedAt()
        );
    }
}
