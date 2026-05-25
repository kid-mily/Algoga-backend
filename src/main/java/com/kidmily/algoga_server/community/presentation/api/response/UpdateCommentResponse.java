package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 수정 응답")
public record UpdateCommentResponse(
        @Schema(description = "수정된 댓글 ID", example = "1")
        Long commentId
) {
}
