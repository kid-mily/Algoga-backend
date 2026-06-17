package com.kidmily.algoga_server.blacklist.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RegisterBlacklistRequest(
    @Schema(description = "블랙리스트에 등록하는 상세 사유", example = "운영정책 5회 이상 위반 및 지속적인 악성 스팸 댓글 게시")
    @NotBlank(message = "블랙리스트 등록 사유는 누락될 수 없습니다.")
    String reason
) {}