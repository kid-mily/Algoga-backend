package com.kidmily.algoga_server.example.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예시 데이터 생성 응답")
public record ExampleResponse(
        @Schema(description = "생성된 예시의 고유 식별자(ID)", example = "1")
        Long exampleId
) {
}