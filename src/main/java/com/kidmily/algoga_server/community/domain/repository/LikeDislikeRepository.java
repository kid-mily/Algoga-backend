package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.TargetType;

public interface LikeDislikeRepository {
    Long countLikes(TargetType targetType, Long targetId);
    Long countDislikes(TargetType targetType, Long targetId);
}