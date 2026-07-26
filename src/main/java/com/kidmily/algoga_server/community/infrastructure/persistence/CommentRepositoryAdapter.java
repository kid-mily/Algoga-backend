package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.infrastructure.mapper.CommentMapper;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.CommentJpaEntity;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<Comment> findExpiredDeletedComments(LocalDateTime threshold) {
        return springDataRepository.findByIsDeletedTrueAndDeletedAtBefore(threshold)
                .stream()
                .map(commentMapper::toDomain)
                .toList();
    }

    @Override
    public Map<Long, Long> countMyCommentsForUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> counts = new HashMap<>();
        for (Long id : userIds) {
            counts.put(id, 0L);
        }
        for (Object[] row : springDataRepository.countMyCommentsForUsers(userIds)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }
}