package com.kidmily.algoga_server.chatbot.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 챗봇 답변 1건에서 근거로 '채택된' 규정 출처(문서/페이지) 1개의 사용 기록.
 * 답변당 (source, page) 기준으로 중복 제거된 1건씩 저장되어, 문서/페이지별 채택 빈도 집계의 원천이 된다.
 */
@Getter
public class RagSourceUsage {
    private final Long ragSourceUsageId;
    private final Long chatLogId;   // 어떤 대화 로그에서 채택됐는지(드릴다운용, nullable)
    private final String source;    // 규정 문서명
    private final Integer page;     // 사람이 읽는 페이지(1-based), 없으면 null
    private final Instant createdAt;

    @Builder
    private RagSourceUsage(Long ragSourceUsageId, Long chatLogId, String source, Integer page, Instant createdAt) {
        this.ragSourceUsageId = ragSourceUsageId;
        this.chatLogId = chatLogId;
        this.source = source;
        this.page = page;
        this.createdAt = createdAt;
    }

    public static RagSourceUsage create(Long chatLogId, String source, Integer page) {
        return RagSourceUsage.builder()
                .chatLogId(chatLogId)
                .source(source)
                .page(page)
                .createdAt(Instant.now())
                .build();
    }

    public static RagSourceUsage reconstitute(Long ragSourceUsageId, Long chatLogId, String source, Integer page, Instant createdAt) {
        return RagSourceUsage.builder()
                .ragSourceUsageId(ragSourceUsageId)
                .chatLogId(chatLogId)
                .source(source)
                .page(page)
                .createdAt(createdAt)
                .build();
    }
}
