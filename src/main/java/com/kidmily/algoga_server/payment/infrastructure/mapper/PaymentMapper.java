package com.kidmily.algoga_server.payment.infrastructure.mapper;

import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.infrastructure.persistence.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentJpaEntity toJpaEntity(Payment payment){
        return new PaymentJpaEntity(
                payment.getBookingId(),
                payment.getCourseId(),
                payment.getUserId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getUsedMileage(),
                payment.getUsedCouponId(),
                payment.getStatus(),
                payment.getIdempotencyKey(),
                payment.getPortonePaymentId(),
                payment.getPaymentMethod(),
                payment.getUserName(),
                payment.getCreatedAt()
        );
    }
    public Payment toDomain(PaymentJpaEntity entity) {
        return  Payment.reconstitute(
                entity.getId(),
                entity.getBookingId(),
                entity.getCourseId(),
                entity.getUserId(),
                entity.getPaymentType(),
                entity.getAmount(),
                entity.getUsedMileage() != null ? entity.getUsedMileage() : 0,
                entity.getUsedCouponId(),
                entity.getStatus(),
                entity.getIdempotencyKey(),
                entity.getPortonePaymentId(),
                entity.getPaymentMethod(),
                entity.getUserName(),
                entity.getCreatedAt()
        );
    }
}
