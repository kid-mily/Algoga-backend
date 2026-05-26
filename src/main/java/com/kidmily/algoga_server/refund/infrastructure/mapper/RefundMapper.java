package com.kidmily.algoga_server.refund.infrastructure.mapper;

import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.infrastructure.persistence.RefundJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RefundMapper {

    public RefundJpaEntity toJpaEntity(RefundRequest refundRequest) {
        return new RefundJpaEntity(
                refundRequest.getId(),
                refundRequest.getBookingId(),
                refundRequest.getPaymentId(),
                refundRequest.getUserId(),
                refundRequest.getStatus(),
                refundRequest.getReason(),
                refundRequest.getRejectReason(),
                refundRequest.getAmount(),
                refundRequest.getCreatedAt(),
                refundRequest.getUpdatedAt()
        );
    }

    public RefundRequest toDomain(RefundJpaEntity entity) {
        return RefundRequest.reconstitute(
                entity.getId(),
                entity.getBookingId(),
                entity.getPaymentId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getReason(),
                entity.getRejectReason(),
                entity.getAmount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}