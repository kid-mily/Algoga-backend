package com.kidmily.algoga_server.community.infrastructure.persistence.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.CommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCommentRepository extends JpaRepository<CommentJpaEntity, Long> {
    Long countByPostIdAndIsDeletedFalse(Long postId);

    List<CommentJpaEntity> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);
}