package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.exception.CommentException;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.infrastructure.mapper.CommentMapper;  // ← 경로 변경
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.CommentJpaEntity;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final SpringDataCommentRepository springDataRepository;
    private final CommentMapper commentMapper;

    @Override
    public Comment save(Comment comment) {
        return commentMapper.toDomain(
                springDataRepository.save(commentMapper.toJpaEntity(comment))
        );
    }

    @Override
    public List<Comment> findActiveCommentsByPostId(Long postId) {
        return springDataRepository
                .findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId)
                .stream()
                .map(commentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return springDataRepository.findById(commentId)
                .map(commentMapper::toDomain);
    }

    @Override
    public Comment update(Comment comment) {
        CommentJpaEntity entity = springDataRepository.findById(comment.getCommentId())
                .orElseThrow();
        entity.updateContent(comment.getContent());
        return commentMapper.toDomain(entity);
    }

    @Override
    public void delete(Comment comment) {
        CommentJpaEntity entity = springDataRepository.findById(comment.getCommentId())
                .orElseThrow();
        entity.softDelete();
    }

    @Override
    public List<Comment> findActiveRepliesByParentId(Long parentId) {
        return springDataRepository.findByParentIdAndIsDeletedFalse(parentId)
                .stream()
                .map(commentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Comment> findAllByPostId(Long postId) {
        return springDataRepository.findByPostId(postId)
                .stream()
                .map(commentMapper::toDomain)
                .toList();
    }

    @Override
    public void softDeleteAll(List<Comment> comments) {
        comments.forEach(comment -> {
            CommentJpaEntity entity = springDataRepository.findById(comment.getCommentId())
                    .orElseThrow();
            entity.softDelete();
        });
    }

    @Override
    public List<Comment> findMyCommentsByPage(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return springDataRepository.findMyCommentsByPage(userId, pageable)
                .stream()
                .map(commentMapper::toDomain)
                .toList();
    }

    @Override
    public long countMyComments(Long userId) {
        return springDataRepository.countMyComments(userId);
    }
}