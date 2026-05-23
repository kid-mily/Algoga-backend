package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 작성 응답")
public record CreatePostResponse(
        @Schema(description = "생성된 게시글 고유 식별자(ID)", example = "1")
        Long postId
) {
}
