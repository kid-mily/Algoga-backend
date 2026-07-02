package com.kidmily.algoga_server.chatbot.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RegisterKnowledgeRequest(
        @NotBlank(message = "정책/규정 내용은 필수입니다.")
        @Schema(description = "실제 정책, 규정 내용 (RAG 컨텍스트로 LLM에 주입될 텍스트)", example = "결제 후 7일 이내, 1강 이하 수강 시 전액 환불 가능합니다.")
        String content,

        @NotEmpty(message = "최소 1개 이상의 예상 질문을 등록해야 합니다.")
        @Schema(description = "사용자가 질문할 법한 예상 질문 목록 (Vector DB에 임베딩될 텍스트들)", example = "[\"환불 규정이 어떻게 되나요?\", \"결제 취소하고 싶어요\", \"수강 철회 방법 알려주세요\"]")
        List<String> expectedQueries
) {}