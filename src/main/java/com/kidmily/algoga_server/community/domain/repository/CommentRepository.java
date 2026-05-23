package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.CommentJpaEntity;

import java.util.List;

public interface CommentRepository {
    List<CommentJpaEntity> findActiveCommentsByPostId(Long postId);
}