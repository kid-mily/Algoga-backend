package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 수정 응답")
public record UpdatePostResponse(
        @Schema(description = "수정된 게시글 ID", example = "1")
        Long postId
) {}