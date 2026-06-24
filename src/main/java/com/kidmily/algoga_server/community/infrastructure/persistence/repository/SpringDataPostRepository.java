package com.kidmily.algoga_server.community.infrastructure.persistence.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostJpaEntity;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {

    @Query("SELECT DISTINCT p FROM PostJpaEntity p " +
            "LEFT JOIN p.postTags t " +
            "WHERE p.isDeleted = false " +
            "AND (:lastPostId IS NULL OR p.postId < :lastPostId) " +
            "AND (:categories IS NULL OR t.tagType IN :categories) " +
            "AND (:countryId IS NULL OR p.countryId = :countryId) " +
            "ORDER BY p.postId DESC")
    List<PostJpaEntity> findPostsByCursor(
            @Param("lastPostId") Long lastPostId,
            @Param("categories") List<PostTagType> categories,
            @Param("countryId") Long countryId,
            Pageable pageable);

    // 💡 마이페이지 전용 단일 통합 쿼리
    @Query("SELECT DISTINCT p FROM PostJpaEntity p " +
            "LEFT JOIN p.postTags t " +
            "WHERE p.isDeleted = false " +
            "AND p.authorId = :authorId " +
            "AND (:lastPostId IS NULL OR p.postId < :lastPostId) " +
            "AND (:categories IS NULL OR t.tagType IN :categories) " +
            "ORDER BY p.postId DESC")
    List<PostJpaEntity> findMyPostsByCursor(
            @Param("authorId") Long authorId,
            @Param("lastPostId") Long lastPostId,
            @Param("categories") List<PostTagType> categories,
            Pageable pageable);

    interface CountryTagCountProjection {
        Long getCountryId();
        long getPostCount();
    }

    @Query("SELECT p.countryId AS countryId, COUNT(p) AS postCount " +
            "FROM PostJpaEntity p " +
            "WHERE p.isDeleted = false AND p.countryId IS NOT NULL " +
            "GROUP BY p.countryId " +
            "ORDER BY COUNT(p) DESC")
    List<CountryTagCountProjection> findTopCountryTags(Pageable pageable);

    // 💡 관리자용 유저별 게시글 페이지 번호 조회
    @Query("SELECT DISTINCT p FROM PostJpaEntity p " +
            "LEFT JOIN p.postTags t " +
            "WHERE p.isDeleted = false " +
            "AND p.authorId = :authorId " +
            "AND (:categories IS NULL OR t.tagType IN :categories) " +
            "ORDER BY p.postId DESC")
    List<PostJpaEntity> findMyPostsByPage(
            @Param("authorId") Long authorId,
            @Param("categories") List<PostTagType> categories,
            Pageable pageable);

    // 💡 관리자용 유저별 게시글 전체 개수 조회
    @Query("SELECT COUNT(DISTINCT p) FROM PostJpaEntity p " +
            "LEFT JOIN p.postTags t " +
            "WHERE p.isDeleted = false " +
            "AND p.authorId = :authorId " +
            "AND (:categories IS NULL OR t.tagType IN :categories)")
    long countMyPosts(
            @Param("authorId") Long authorId,
            @Param("categories") List<PostTagType> categories);

    List<PostJpaEntity> findByIsDeletedTrueAndDeletedAtBefore(LocalDateTime threshold);

    @Modifying
    @Query("UPDATE PostJpaEntity p SET p.viewCount = p.viewCount + :count WHERE p.postId = :postId")
    void increaseViewCount(@Param("postId") Long postId, @Param("count") long count);

}