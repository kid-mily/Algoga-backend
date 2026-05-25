package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.domain.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);
    List<Comment> findActiveCommentsByPostId(Long postId);
    Comment update(Comment comment);
    void delete(Comment comment);
    Optional<Comment> findById(Long commentId);
    List<Comment> findActiveRepliesByParentId(Long parentId);
}