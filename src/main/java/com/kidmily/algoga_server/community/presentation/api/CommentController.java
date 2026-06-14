package com.kidmily.algoga_server.community.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.community.application.command.AdminDeleteCommentCommand;
import com.kidmily.algoga_server.community.application.command.DeleteCommentCommand;
import com.kidmily.algoga_server.community.application.command.UpdateCommentCommand;
import com.kidmily.algoga_server.community.application.usecase.CommentCommandUseCase;
import com.kidmily.algoga_server.community.application.usecase.CommentQueryUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.community.presentation.api.request.UpdateCommentRequest;
import com.kidmily.algoga_server.community.presentation.api.response.AdminCommentListItemResponse;
import com.kidmily.algoga_server.community.presentation.api.response.AdminCommentListResponse;
import com.kidmily.algoga_server.community.presentation.api.response.UpdateCommentResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@Tag(name = "Community", description = "커뮤니티 도메인 API")
public class CommentController {

    private final CommentCommandUseCase commentCommandUseCase;
    private final CommentQueryUseCase commentQueryUseCase;

    @PatchMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "본인 댓글의 내용을 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 수정에 성공했습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "COMMENT_NOT_FOUND",
            "COMMENT_UPDATE_FORBIDDEN",
            "ACCOUNT_TYPE_FORBIDDEN"
    })
    public ResponseEntity<ApiResponse<UpdateCommentResponse>> updateComment(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new PostException(PostErrorCode.ACCOUNT_TYPE_FORBIDDEN);
        }
        Long currentUserId = userDetails.getUser().getId();


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
            "ACCOUNT_TYPE_FORBIDDEN",
            "COMMENT_DELETE_FORBIDDEN"
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
            @AuthenticationPrincipal Object principal
    ) {
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new PostException(PostErrorCode.ACCOUNT_TYPE_FORBIDDEN);
        }
        Long currentUserId = userDetails.getUser().getId();


        DeleteCommentCommand command = new DeleteCommentCommand(commentId, currentUserId);
        commentCommandUseCase.handle(command);

        return ResponseEntity.noContent().build();
    }

    // 관리자의 유저별 댓글 목록 조회 (페이징)
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/admin/users/{userId}")
    @Operation(summary = "유저별 댓글 목록 조회 (관리자용)", description = "CS 관리자가 특정 유저가 작성한 댓글 목록을 페이지 번호 기반으로 조회합니다.")
    public ResponseEntity<ApiResponse<AdminCommentListResponse>> getUserComments(
            @PathVariable Long userId,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") Integer index,
            @CurrentManager Long managerId
    ) {
        AdminCommentListResponse responseData = commentQueryUseCase.getMyCommentsByPage(userId, index);
        return ResponseEntity.ok(ApiResponse.success("ADMIN_USER_COMMENTS_FOUND", "유저별 댓글 목록 조회에 성공했습니다.", responseData));
    }

    // 관리자의 댓글 상세 조회
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/admin/{commentId}")
    @Operation(summary = "댓글 상세 조회 (관리자용)", description = "CS 관리자가 댓글과 소속 게시글 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {"COMMENT_NOT_FOUND", "POST_NOT_FOUND"})
    public ResponseEntity<ApiResponse<AdminCommentListItemResponse>> getCommentForAdmin(  // 반환 타입 변경
                                                                                          @PathVariable Long commentId,
                                                                                          @CurrentManager Long managerId
    ) {
        AdminCommentListItemResponse responseData = commentQueryUseCase.getCommentForAdmin(commentId);  // 변경
        return ResponseEntity.ok(ApiResponse.success("ADMIN_COMMENT_FOUND", "댓글 상세 조회에 성공했습니다.", responseData));
    }

    // 관리자의 댓글 삭제 (소프트 딜리트, 대댓글 포함)
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @DeleteMapping("/admin/{commentId}")
    @Operation(summary = "댓글 삭제 (관리자용)", description = "CS 관리자가 부적절한 댓글을 Soft Delete 처리합니다. 일반 댓글인 경우 대댓글도 함께 삭제됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "댓글이 성공적으로 삭제되었습니다. (반환 바디 없음)")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {"COMMENT_NOT_FOUND"})
    public ResponseEntity<Void> deleteCommentByAdmin(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
            @CurrentManager Long managerId
    ) {
        commentCommandUseCase.handle(new AdminDeleteCommentCommand(commentId));
        return ResponseEntity.noContent().build();
    }
}