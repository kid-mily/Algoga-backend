package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.command.ToggleReactionCommand;
import com.kidmily.algoga_server.community.application.usecase.ReactionCommandUseCase;
import com.kidmily.algoga_server.community.domain.model.LikeDislike;
import com.kidmily.algoga_server.community.domain.model.TargetType;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.LikeDislikeRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.CommentException;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.community.settings.cache.CommunityCacheType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReactionCommandService implements ReactionCommandUseCase {

    private final LikeDislikeRepository likeDislikeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CacheManager cacheManager;

    @Override
    public ReactionResult handle(ToggleReactionCommand command) {

        // 대상 존재 여부 검증
        if (command.targetType() == TargetType.POST) {
            postRepository.findById(command.targetId())
                    .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
        } else if (command.targetType() == TargetType.COMMENT) {
            commentRepository.findById(command.targetId())
                    .orElseThrow(() -> new CommentException(PostErrorCode.COMMENT_NOT_FOUND));
        }


        log.info("[ReactionCommandService] 반응 토글 요청 - userId: {}, targetType: {}, targetId: {}, isLike: {}",
                command.userId(), command.targetType(), command.targetId(), command.isLike());

        Optional<LikeDislike> existing = likeDislikeRepository.findByUserAndTarget(
                command.userId(), command.targetType(), command.targetId()
        );

        ReactionStatus status;

        if (existing.isEmpty()) {
            likeDislikeRepository.save(LikeDislike.create(
                    command.userId(), command.targetType(), command.targetId(), command.isLike()
            ));
            log.info("[ReactionCommandService] 반응 추가 완료");
            status = ReactionStatus.ADDED;

        } else {
            LikeDislike current = existing.get();

            if (current.isSameReaction(command.isLike())) {
                likeDislikeRepository.delete(current);
                log.info("[ReactionCommandService] 반응 취소 완료");
                status = ReactionStatus.REMOVED;

            } else {
                likeDislikeRepository.delete(current);
                likeDislikeRepository.save(LikeDislike.create(
                        command.userId(), command.targetType(), command.targetId(), command.isLike()
                ));
                log.info("[ReactionCommandService] 반응 전환 완료");
                status = ReactionStatus.CHANGED;
            }
        }

        Long likeCount = likeDislikeRepository.countLikes(command.targetType(), command.targetId());
        Long dislikeCount = likeDislikeRepository.countDislikes(command.targetType(), command.targetId());

        // 게시글 좋아요일 때만 해당 게시글 캐시 무효화
        if (command.targetType() == TargetType.POST) {
            Cache cache = cacheManager.getCache(CommunityCacheType.Const.POST_DETAIL);
            if (cache != null) {
                cache.evict(command.targetId());   // targetId == postId
            }
        }

        return new ReactionResult(status, likeCount, dislikeCount);
    }
}