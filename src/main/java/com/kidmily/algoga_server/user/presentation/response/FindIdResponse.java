package com.kidmily.algoga_server.user.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마스킹된 아이디 응답")
public record FindIdResponse(
        @Schema(description = "마스킹된 아이디", example = "alg***")
        String maskedId
) {}