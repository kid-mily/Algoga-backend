package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.domain.model.TargetType;

public interface LikeDislikeRepository {
    Long countLikes(TargetType targetType, Long targetId);
    Long countDislikes(TargetType targetType, Long targetId);
}