package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.domain.model.LikeDislike;
import com.kidmily.algoga_server.community.domain.model.TargetType;

import java.util.Optional;

public interface LikeDislikeRepository {
    Long countLikes(TargetType targetType, Long targetId);
    Long countDislikes(TargetType targetType, Long targetId);
    Optional<LikeDislike> findByUserAndTarget(Long userId, TargetType targetType, Long targetId);
    LikeDislike save(LikeDislike likeDislike);
    void delete(LikeDislike likeDislike);
}