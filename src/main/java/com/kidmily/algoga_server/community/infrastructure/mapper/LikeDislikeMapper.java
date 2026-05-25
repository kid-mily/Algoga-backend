package com.kidmily.algoga_server.community.infrastructure.mapper;

import com.kidmily.algoga_server.community.domain.model.LikeDislike;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.LikeDislikeJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LikeDislikeMapper {

    default LikeDislikeJpaEntity toJpaEntity(LikeDislike likeDislike) {
        if (likeDislike == null) return null;
        return LikeDislikeJpaEntity.builder()
                .userId(likeDislike.getUserId())
                .targetType(likeDislike.getTargetType())
                .targetId(likeDislike.getTargetId())
                .isLike(likeDislike.getIsLike())
                .build();
    }

    default LikeDislike toDomain(LikeDislikeJpaEntity entity) {
        if (entity == null) return null;
        return LikeDislike.reconstitute(
                entity.getLikeId(),
                entity.getUserId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getIsLike(),
                entity.getCreatedAt()
        );
    }
}