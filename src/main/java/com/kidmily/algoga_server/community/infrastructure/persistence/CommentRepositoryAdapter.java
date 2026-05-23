package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.CommentJpaEntity;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final SpringDataCommentRepository springDataRepository;

    @Override
    public List<CommentJpaEntity> findActiveCommentsByPostId(Long postId) {
        return springDataRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
    }
}
