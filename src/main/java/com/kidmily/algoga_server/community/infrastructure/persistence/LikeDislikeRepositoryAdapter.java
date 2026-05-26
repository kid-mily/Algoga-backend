package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.domain.model.LikeDislike;
import com.kidmily.algoga_server.community.domain.model.TargetType;
import com.kidmily.algoga_server.community.domain.repository.LikeDislikeRepository;
import com.kidmily.algoga_server.community.infrastructure.mapper.LikeDislikeMapper;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataLikeDislikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikeDislikeRepositoryAdapter implements LikeDislikeRepository {

    private final SpringDataLikeDislikeRepository springDataRepository;
    private final LikeDislikeMapper likeDislikeMapper;

    @Override
    public Long countLikes(TargetType targetType, Long targetId) {
        return springDataRepository.countByTargetTypeAndTargetIdAndLike(targetType, targetId, true);
    }

    @Override
    public Long countDislikes(TargetType targetType, Long targetId) {
        return springDataRepository.countByTargetTypeAndTargetIdAndLike(targetType, targetId, false);
    }

    @Override
    public Optional<LikeDislike> findByUserAndTarget(Long userId, TargetType targetType, Long targetId) {
        return springDataRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .map(likeDislikeMapper::toDomain);
    }

    @Override
    public LikeDislike save(LikeDislike likeDislike) {
        return likeDislikeMapper.toDomain(
                springDataRepository.save(likeDislikeMapper.toJpaEntity(likeDislike))
        );
    }

    @Override
    public void delete(LikeDislike likeDislike) {
        springDataRepository.deleteById(likeDislike.getLikeId());
    }
}