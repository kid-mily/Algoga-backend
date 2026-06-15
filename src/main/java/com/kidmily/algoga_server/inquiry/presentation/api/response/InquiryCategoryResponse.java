// inquiry/presentation/api/response/InquiryCategoryResponse.java
package com.kidmily.algoga_server.inquiry.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문의 카테고리(태그) 응답 DTO")
public record InquiryCategoryResponse(
        @Schema(description = "카테고리 코드 (서버 전송용)", example = "REFUND")
        String code,

        @Schema(description = "카테고리 표시명 (화면 노출용)", example = "환불")
        String description
) {}