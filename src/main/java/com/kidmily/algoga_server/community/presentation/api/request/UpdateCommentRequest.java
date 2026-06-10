package com.kidmily.algoga_server.community.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청")
public record UpdateCommentRequest(
        @Schema(description = "수정할 내용 (최대 500자)", example = "엔화는 환전하고 가시는거 추천해요", maxLength = 500)
        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 500, message = "댓글은 최대 500자입니다.")
        String content
) {

}
