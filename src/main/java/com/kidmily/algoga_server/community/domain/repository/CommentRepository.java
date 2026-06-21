package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.domain.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);
    List<Comment> findActiveCommentsByPostId(Long postId);
    Comment update(Comment comment);
    void delete(Comment comment);
    Optional<Comment> findById(Long commentId);
    List<Comment> findActiveRepliesByParentId(Long parentId);
    List<Comment> findAllByPostId(Long postId);
    void softDeleteAll(List<Comment> comments);
    List<Comment> findMyCommentsByPage(Long userId, int page, int size);
    long countMyComments(Long userId);
    List<Comment> findExpiredDeletedComments(LocalDateTime threshold);
}