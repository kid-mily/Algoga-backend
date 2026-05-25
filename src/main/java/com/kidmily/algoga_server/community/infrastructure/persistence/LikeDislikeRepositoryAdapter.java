package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.domain.repository.LikeDislikeRepository;
import com.kidmily.algoga_server.community.domain.model.TargetType;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataLikeDislikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikeDislikeRepositoryAdapter implements LikeDislikeRepository {

    private final SpringDataLikeDislikeRepository springDataRepository;

    @Override
    public Long countLikes(TargetType targetType, Long targetId) {
        return springDataRepository.countByTargetTypeAndTargetIdAndLike(targetType, targetId, true);
    }

    @Override
    public Long countDislikes(TargetType targetType, Long targetId) {
        return springDataRepository.countByTargetTypeAndTargetIdAndLike(targetType, targetId, false);
    }
}