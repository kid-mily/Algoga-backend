package com.kidmily.algoga_server.community.presentation.api;

import com.kidmily.algoga_server.community.application.command.ToggleReactionCommand;
import com.kidmily.algoga_server.community.application.usecase.ReactionCommandUseCase;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.presentation.api.request.ToggleReactionRequest;
import com.kidmily.algoga_server.community.presentation.api.response.ToggleReactionResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reactions")
@Tag(name = "Community", description = "커뮤니티 도메인 API")
public class ReactionController {

    private final ReactionCommandUseCase reactionCommandUseCase;

    @PostMapping
    @Operation(summary = "좋아요/싫어요 토글", description = "게시글/댓글에 좋아요 또는 싫어요를 토글합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반응 처리에 성공했습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "REACTION_UNAUTHORIZED",
            "POST_NOT_FOUND",
            "COMMENT_NOT_FOUND"
    })
    public ResponseEntity<ApiResponse<ToggleReactionResponse>> toggleReaction(
            @Valid @RequestBody ToggleReactionRequest request
    ) {
        Long currentUserId = ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUser().getId();

        ToggleReactionCommand command = new ToggleReactionCommand(
                currentUserId,
                request.targetType(),
                request.targetId(),
                request.isLike()
        );

        ReactionCommandUseCase.ReactionResult result = reactionCommandUseCase.handle(command);

        ToggleReactionResponse responseData = new ToggleReactionResponse(
                result.status(),
                result.likeCount(),
                result.dislikeCount()
        );

        return ResponseEntity.ok(ApiResponse.success("REACTION_TOGGLED", "반응 처리에 성공했습니다.", responseData));
    }
}