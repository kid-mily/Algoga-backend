package com.kidmily.algoga_server.blacklist.infrastructure.mapper;

import com.kidmily.algoga_server.blacklist.domain.model.Blacklist;
import com.kidmily.algoga_server.blacklist.infrastructure.persistence.entity.BlacklistJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BlacklistMapper {

    public BlacklistJpaEntity toJpaEntity(Blacklist domain) {
        if (domain == null) return null;
        return BlacklistJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .reason(domain.getReason())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .unblacklistedAt(domain.getUnblacklistedAt())
                .build();
    }

    public Blacklist toDomain(BlacklistJpaEntity entity) {
        if (entity == null) return null;
        return Blacklist.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .unblacklistedAt(entity.getUnblacklistedAt())
                .build();
    }
}