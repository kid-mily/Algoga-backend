package com.kidmily.algoga_server.community.presentation.api.response;
import io.swagger.v3.oas.annotations.media.Schema;
import com.kidmily.algoga_server.community.application.usecase.ReactionCommandUseCase.ReactionStatus;

@Schema(description = "좋아요/싫어요 응답")
public record ToggleReactionResponse(
        @Schema(description = "처리 결과 (ADDED, REMOVED, CHANGED)", example = "ADDED")
        ReactionStatus status,

        @Schema(description = "좋아요 수", example = "10")
        Long likeCount,

        @Schema(description = "싫어요 수", example = "2")
        Long dislikeCount
) {}