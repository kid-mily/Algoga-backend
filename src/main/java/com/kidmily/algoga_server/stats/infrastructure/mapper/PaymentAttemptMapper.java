package com.kidmily.algoga_server.stats.infrastructure.mapper;

import com.kidmily.algoga_server.stats.domain.model.PaymentAttempt;
import com.kidmily.algoga_server.stats.infrastructure.persistence.PaymentAttemptJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentAttemptMapper {

    public PaymentAttemptJpaEntity toJpaEntity(PaymentAttempt domain) {
        return new PaymentAttemptJpaEntity(
                domain.getUserId(),
                domain.getAccommodationId(),
                domain.getCreatedAt()
        );
    }

    public PaymentAttempt toDomain(PaymentAttemptJpaEntity entity) {
        return PaymentAttempt.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getAccommodationId(),
                entity.getCreatedAt()
        );
    }
}
