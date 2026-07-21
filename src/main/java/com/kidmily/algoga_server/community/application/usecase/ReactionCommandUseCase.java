package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.application.command.ToggleReactionCommand;


public interface ReactionCommandUseCase {
    ReactionResult handle(ToggleReactionCommand command);

    enum ReactionStatus {
        ADDED,
        REMOVED,
        CHANGED
    }

    record ReactionResult(
            ReactionStatus status,
            Long likeCount,
            Long dislikeCount
    ) {}
}