package com.kidmily.algoga_server.community.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 생성 요청")
public record CreateCommentRequest(

        @Schema(description = "부모 댓글 ID (대댓글일 경우 입력, 일반 댓글은 null)", example = "null")
        Long parentId,

        @Schema(description = "댓글 내용", example = "엔화는 환전하고 가시는거 추천해요", maxLength = 500)
        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 500, message = "댓글은 최대 500자입니다.")
        String content
) {}