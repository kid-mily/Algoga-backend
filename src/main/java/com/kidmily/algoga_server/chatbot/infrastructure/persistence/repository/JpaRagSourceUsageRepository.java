package com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.RagSourceUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface JpaRagSourceUsageRepository extends JpaRepository<RagSourceUsageEntity, Long> {

    /** 문서/페이지별 채택 빈도 집계. from/toExclusive 가 null 이면 기간 조건 무시. */
    @Query("SELECT r.source AS source, r.page AS page, COUNT(r) AS count "
            + "FROM RagSourceUsageEntity r "
            + "WHERE (:from IS NULL OR r.createdAt >= :from) "
            + "AND (:toExclusive IS NULL OR r.createdAt < :toExclusive) "
            + "GROUP BY r.source, r.page "
            + "ORDER BY COUNT(r) DESC")
    List<SourceStatProjection> aggregate(@Param("from") Instant from, @Param("toExclusive") Instant toExclusive);

    interface SourceStatProjection {
        String getSource();
        Integer getPage();
        long getCount();
    }
}
