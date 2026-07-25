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
                .favorite(entity.isFavorite())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public FriendJpaEntity toEntity(FriendRelation domain) {
        if (domain == null) return null;
        // createdAt/updatedAt을 안 넣으면, 기존 관계를 수정(save)할 때 JPA가 이 detached 엔티티를
        // merge하면서 메모리에 없는(null인) createdAt을 그대로 DB에 덮어써버린다(updatedAt은 @LastModifiedDate가
        // @PreUpdate 시점에 새로 채워주지만, createdAt은 @PrePersist에서만 채워지고 이후엔 안 건드리기 때문).
        // 그래서 도메인 객체가 들고 있는 값을 그대로 넘겨서 이 문제를 막는다.
        return FriendJpaEntity.builder()
                .id(domain.getId())
                .requesterId(domain.getRequesterId())
                .receiverId(domain.getReceiverId())
                .status(domain.getStatus())
                .favorite(domain.isFavorite())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}