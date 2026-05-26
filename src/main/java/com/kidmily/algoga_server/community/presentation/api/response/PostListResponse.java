package com.kidmily.algoga_server.community.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "게시글 목록 응답 (무한 스크롤)")
public record PostListResponse(

        @Schema(description = "게시글 목록")
        List<PostListItemResponse> posts,

        @Schema(description = "다음 페이지가 있는지 여부", example = "true")
        Boolean hasNext,

        @Schema(description = "다음 요청 시 사용할 마지막 게시글 ID (없으면 null)", example = "420")
        Long lastPostId
) {}