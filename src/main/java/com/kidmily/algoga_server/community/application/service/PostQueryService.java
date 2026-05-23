package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.usecase.PostQueryUseCase;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.LikeDislikeRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.TargetType;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataCommentRepository;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataLikeDislikeRepository;
import com.kidmily.algoga_server.community.presentation.api.response.CommentResponse;
import com.kidmily.algoga_server.community.presentation.api.response.PostResponse;
import com.kidmily.algoga_server.community.presentation.api.response.TagResponse;
import com.kidmily.algoga_server.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService implements PostQueryUseCase {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeDislikeRepository likeDislikeRepository;

    @Override
    @Transactional
    public PostResponse getPost(Long postId) {
        log.info("[PostQueryService] 게시글 단건 조회 요청 - postId: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("[PostQueryService] 게시글을 찾을 수 없음 - postId: {}", postId);
                    return new BusinessException(PostErrorCode.POST_NOT_FOUND);
                });

        // 조회수 증가
        post.increaseViewCount();
        postRepository.update(post);

        // 좋아요/싫어요 수 카운트
        Long likeCount = likeDislikeRepository.countLikes(TargetType.POST, postId);
        Long dislikeCount = likeDislikeRepository.countDislikes(TargetType.POST, postId);


        List<CommentResponse> comments = commentRepository
                .findActiveCommentsByPostId(postId)
                .stream()
                .map(c -> new CommentResponse(c.getCommentId(), c.getUserId(), c.getContent(), c.getCreatedAt()))
                .toList();

        // 💡 [수정] 태그 변환 로직을 메서드 안쪽으로 이동시켰습니다.
        Stream<TagResponse> categoryStream = post.getCategory() != null ?
                Stream.of(TagResponse.fromCategory(post.getCategory())) : Stream.empty();

        Stream<TagResponse> freeTagStream = post.getFreeTags() != null ?
                post.getFreeTags().stream().map(TagResponse::fromFreeTag) : Stream.empty();

        List<TagResponse> tags = Stream.concat(categoryStream, freeTagStream).toList();

        return new PostResponse(
                post.getId(),
                post.getAuthorId(),
                tags,
                post.getTitle(),
                post.getContent(),
                post.getCountryId(),
                post.getLectureId(),
                post.getImageUrls(),
                post.getViewCount(),
                likeCount,
                dislikeCount,
                (long) comments.size(),
                comments,
                post.getCreatedAt()
        );
    }


}