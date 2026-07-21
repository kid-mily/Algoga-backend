package com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ChatLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface JpaChatLogRepository extends JpaRepository<ChatLogEntity, Long> {
    List<ChatLogEntity> findByUserIdOrderByCreatedAtAsc(Long userId, Pageable pageable);
    List<ChatLogEntity> findByUserIdAndChatLogIdLessThanOrderByCreatedAtAsc(Long userId, Long beforeChatLogId, Pageable pageable);

    // 어드민 대화 로그 검색. 각 파라미터가 null 이면 해당 조건을 건너뛴다.
    @Query(value = "SELECT c FROM ChatLogEntity c "
            + "WHERE (:isFiltered IS NULL OR c.isFiltered = :isFiltered) "
            + "AND (:from IS NULL OR c.createdAt >= :from) "
            + "AND (:toExclusive IS NULL OR c.createdAt < :toExclusive) "
            + "AND (:keyword IS NULL "
            + "     OR c.question LIKE CONCAT('%', :keyword, '%') "
            + "     OR c.answer LIKE CONCAT('%', :keyword, '%')) "
            + "ORDER BY c.createdAt DESC",
            countQuery = "SELECT COUNT(c) FROM ChatLogEntity c "
            + "WHERE (:isFiltered IS NULL OR c.isFiltered = :isFiltered) "
            + "AND (:from IS NULL OR c.createdAt >= :from) "
            + "AND (:toExclusive IS NULL OR c.createdAt < :toExclusive) "
            + "AND (:keyword IS NULL "
            + "     OR c.question LIKE CONCAT('%', :keyword, '%') "
            + "     OR c.answer LIKE CONCAT('%', :keyword, '%'))")
    Page<ChatLogEntity> searchForAdmin(
            @Param("isFiltered") Boolean isFiltered,
            @Param("from") Instant from,
            @Param("toExclusive") Instant toExclusive,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // CSV 내보내기용: 페이징 없이 필터 조건에 맞는 전체 로그(최신순).
    @Query("SELECT c FROM ChatLogEntity c "
            + "WHERE (:isFiltered IS NULL OR c.isFiltered = :isFiltered) "
            + "AND (:from IS NULL OR c.createdAt >= :from) "
            + "AND (:toExclusive IS NULL OR c.createdAt < :toExclusive) "
            + "AND (:keyword IS NULL "
            + "     OR c.question LIKE CONCAT('%', :keyword, '%') "
            + "     OR c.answer LIKE CONCAT('%', :keyword, '%')) "
            + "ORDER BY c.createdAt DESC")
    List<ChatLogEntity> searchAllForAdmin(
            @Param("isFiltered") Boolean isFiltered,
            @Param("from") Instant from,
            @Param("toExclusive") Instant toExclusive,
            @Param("keyword") String keyword
    );
}