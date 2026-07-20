package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.RagSourceUsage;

import java.time.Instant;
import java.util.List;

public interface RagSourceUsageRepository {

    void saveAll(List<RagSourceUsage> usages);

    /**
     * 문서/페이지별 채택 빈도 집계(내림차순). from/toExclusive 가 null 이면 기간 조건을 무시한다.
     */
    List<SourceStat> aggregate(Instant from, Instant toExclusive);

    /** 집계 결과 한 행: 문서/페이지 + 채택 횟수. */
    record SourceStat(String source, Integer page, long count) {}
}
