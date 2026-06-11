package com.kidmily.algoga_server.community.infrastructure.persistence.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.CommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCommentRepository extends JpaRepository<CommentJpaEntity, Long> {
    List<CommentJpaEntity> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);
    List<CommentJpaEntity> findByParentIdAndIsDeletedFalse(Long parentId);
    List<CommentJpaEntity> findByPostId(Long postId); // 삭제 여부 상관없이 전체 조회
}