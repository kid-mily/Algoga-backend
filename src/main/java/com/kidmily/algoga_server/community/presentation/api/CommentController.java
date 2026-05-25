package com.kidmily.algoga_server.community.presentation.api;

import com.kidmily.algoga_server.community.application.command.DeleteCommentCommand;
import com.kidmily.algoga_server.community.application.command.UpdateCommentCommand;
import com.kidmily.algoga_server.community.application.usecase.CommentCommandUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.presentation.api.request.UpdateCommentRequest;
import com.kidmily.algoga_server.community.presentation.api.response.UpdateCommentResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@Tag(name = "Community", description = "커뮤니티 도메인 API")
public class CommentController {

    private final CommentCommandUseCase commentCommandUseCase;

    @PatchMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "본인 댓글의 내용을 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 수정에 성공했습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "COMMENT_NOT_FOUND",
            "COMMENT_UNAUTHORIZED",
            "COMMENT_UPDATE_FORBIDDEN"
    })
    public ResponseEntity<ApiResponse<UpdateCommentResponse>> updateComment(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        Long currentUserId = 1L;

        UpdateCommentCommand command = new UpdateCommentCommand(commentId, currentUserId, request.content());
        Comment updatedComment = commentCommandUseCase.handle(command);

        return ResponseEntity.ok(ApiResponse.success("COMMENT_UPDATED", "댓글 수정에 성공했습니다.",
                new UpdateCommentResponse(updatedComment.getCommentId())));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "본인 댓글을 Soft Delete 처리합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "댓글이 성공적으로 삭제되었습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "COMMENT_NOT_FOUND",
            "COMMENT_UNAUTHORIZED",
            "COMMENT_DELETE_FORBIDDEN"
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId
    ) {
        Long currentUserId = 1L;

        DeleteCommentCommand command = new DeleteCommentCommand(commentId, currentUserId);
        commentCommandUseCase.handle(command);

        return ResponseEntity.noContent().build();
    }
}