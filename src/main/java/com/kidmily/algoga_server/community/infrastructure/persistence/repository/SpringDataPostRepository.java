package com.kidmily.algoga_server.community.infrastructure.persistence.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostJpaEntity;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {

    @Query("SELECT DISTINCT p FROM PostJpaEntity p " +
            "LEFT JOIN p.postTags t " +
            "WHERE p.isDeleted = false " +
            "AND (:lastPostId IS NULL OR p.postId < :lastPostId) " +
            "AND (:categories IS NULL OR t.tagType IN :categories) " +
            "ORDER BY p.postId DESC")
    List<PostJpaEntity> findPostsByCursor(
            @Param("lastPostId") Long lastPostId,
            @Param("categories") List<PostTagType> categories,
            Pageable pageable);

    // 💡 마이페이지 전용 단일 통합 쿼리
    @Query("SELECT DISTINCT p FROM PostJpaEntity p " +
            "LEFT JOIN p.postTags t " +
            "WHERE p.isDeleted = false " +
            "AND p.authorId = :authorId " + // 본인 글 필터링 조건
            "AND (:lastPostId IS NULL OR p.postId < :lastPostId) " +
            "AND (:categories IS NULL OR t.tagType IN :categories) " +
            "ORDER BY p.postId DESC")
    List<PostJpaEntity> findMyPostsByCursor(
            @Param("authorId") Long authorId,
            @Param("lastPostId") Long lastPostId,
            @Param("categories") List<PostTagType> categories,
            Pageable pageable);
}