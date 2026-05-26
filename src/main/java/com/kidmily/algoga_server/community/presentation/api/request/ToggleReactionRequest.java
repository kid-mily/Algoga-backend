package com.kidmily.algoga_server.community.presentation.api.request;

import com.kidmily.algoga_server.community.domain.model.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좋아요/싫어요 요청")
public record ToggleReactionRequest(

        @Schema(description = "대상 타입 (POST, COMMENT)", example = "POST")
        @NotNull(message = "대상 타입은 필수입니다.")
        TargetType targetType,

        @Schema(description = "대상 ID", example = "1")
        @NotNull(message = "대상 ID는 필수입니다.")
        Long targetId,

        @Schema(description = "좋아요 여부 (true: 좋아요, false: 싫어요)", example = "true")
        @NotNull(message = "좋아요/싫어요 여부는 필수입니다.")
        Boolean isLike
) {}