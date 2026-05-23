package com.kidmily.algoga_server.community.infrastructure.persistence.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.LikeDislikeJpaEntity;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLikeDislikeRepository extends JpaRepository<LikeDislikeJpaEntity, Long> {
    @Query("SELECT COUNT(l) FROM LikeDislikeJpaEntity l " +
            "WHERE l.targetType = :targetType AND l.targetId = :targetId AND l.isLike = :isLike")
    Long countByTargetTypeAndTargetIdAndLike(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId,
            @Param("isLike") Boolean isLike
    );
}