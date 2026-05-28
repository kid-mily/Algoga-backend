package com.kidmily.algoga_server.friend.infrastructure.mapper;

import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.infrastructure.persistence.entity.FriendJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class FriendMapper {
    public FriendRelation toDomain(FriendJpaEntity entity) {
        if (entity == null) return null;
        return FriendRelation.builder()
                .id(entity.getId())
                .requesterId(entity.getRequesterId())
                .receiverId(entity.getReceiverId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public FriendJpaEntity toEntity(FriendRelation domain) {
        if (domain == null) return null;
        return FriendJpaEntity.builder()
                .id(domain.getId())
                .requesterId(domain.getRequesterId())
                .receiverId(domain.getReceiverId())
                .status(domain.getStatus())
                .build();
    }
}