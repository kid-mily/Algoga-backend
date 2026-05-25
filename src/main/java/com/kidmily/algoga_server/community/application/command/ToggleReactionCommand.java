package com.kidmily.algoga_server.community.application.command;

import com.kidmily.algoga_server.community.domain.model.TargetType;

public record ToggleReactionCommand(
        Long userId,
        TargetType targetType,
        Long targetId,
        Boolean isLike  // true: 좋아요, false: 싫어요
) {
}