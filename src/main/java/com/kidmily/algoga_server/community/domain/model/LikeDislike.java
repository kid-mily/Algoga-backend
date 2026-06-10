package com.kidmily.algoga_server.community.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeDislike {

    private Long likeId;
    private Long userId;
    private TargetType targetType;
    private Long targetId;
    private Boolean isLike;
    private LocalDateTime createdAt;

    private LikeDislike(Long userId, TargetType targetType, Long targetId, Boolean isLike) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.isLike = isLike;
        this.createdAt = LocalDateTime.now();
    }

    private LikeDislike(Long likeId, Long userId, TargetType targetType, Long targetId,
                        Boolean isLike, LocalDateTime createdAt) {
        this.likeId = likeId;
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.isLike = isLike;
        this.createdAt = createdAt;
    }

    // 신규 생성
    public static LikeDislike create(Long userId, TargetType targetType, Long targetId, Boolean isLike) {
        return new LikeDislike(userId, targetType, targetId, isLike);
    }

    // DB 복원
    public static LikeDislike reconstitute(Long likeId, Long userId, TargetType targetType,
                                           Long targetId, Boolean isLike, LocalDateTime createdAt) {
        return new LikeDislike(likeId, userId, targetType, targetId, isLike, createdAt);
    }

    // 같은 타입(좋아요/싫어요)인지 확인
    public boolean isSameReaction(Boolean isLike) {
        return this.isLike.equals(isLike);
    }
}