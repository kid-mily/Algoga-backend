package com.kidmily.algoga_server.community.infrastructure.persistence.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.CommentJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataCommentRepository extends JpaRepository<CommentJpaEntity, Long> {
    List<CommentJpaEntity> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);
    List<CommentJpaEntity> findByParentIdAndIsDeletedFalse(Long parentId);
    List<CommentJpaEntity> findByPostId(Long postId); // 삭제 여부 상관없이 전체 조회
    List<CommentJpaEntity> findByIsDeletedTrueAndDeletedAtBefore(LocalDateTime threshold);
    @Query("SELECT c FROM CommentJpaEntity c WHERE c.userId = :userId AND c.isDeleted = false ORDER BY c.commentId DESC")
    List<CommentJpaEntity> findMyCommentsByPage(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CommentJpaEntity c WHERE c.userId = :userId AND c.isDeleted = false")
    long countMyComments(@Param("userId") Long userId);

    // 관리자용 유저별 댓글 개수 배치 조회 (N+1 방지)
    @Query("SELECT c.userId, COUNT(c) FROM CommentJpaEntity c " +
            "WHERE c.isDeleted = false " +
            "AND c.userId IN :userIds " +
            "GROUP BY c.userId")
    List<Object[]> countMyCommentsForUsers(@Param("userIds") List<Long> userIds);

}