package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.policy.CommunityQueryPolicy;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.model.TargetType;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.LikeDislikeRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.community.presentation.api.response.CommentResponse;
import com.kidmily.algoga_server.community.presentation.api.response.PostResponse;
import com.kidmily.algoga_server.community.presentation.api.response.TagResponse;
import com.kidmily.algoga_server.community.settings.cache.CommunityCacheType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReadService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeDislikeRepository likeDislikeRepository;
    private final CommunityQueryPolicy communityQueryPolicy;

   @Cacheable(cacheNames = CommunityCacheType.Const.POST_DETAIL, key = "#postId")
    public PostResponse getPostContentOnly(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        Long likeCount = likeDislikeRepository.countLikes(TargetType.POST, postId);
        Long dislikeCount = likeDislikeRepository.countDislikes(TargetType.POST, postId);

       List<Comment> activeComments = commentRepository.findActiveCommentsByPostId(postId);
       List<CommentResponse> comments = toCommentTree(activeComments);

        String countryName = communityQueryPolicy.resolveCountryName(post.getCountryId());

        Stream<TagResponse> countryStream = countryName != null ?
                Stream.of(TagResponse.fromCountry(post.getCountryId(), countryName)) : Stream.empty();
        Stream<TagResponse> categoryStream = post.getCategory() != null ?
                Stream.of(TagResponse.fromCategory(post.getCategory())) : Stream.empty();
        Stream<TagResponse> freeTagStream = post.getFreeTags() != null ?
                post.getFreeTags().stream().map(TagResponse::fromFreeTag) : Stream.empty();

        List<TagResponse> tags = Stream.concat(countryStream,
                Stream.concat(categoryStream, freeTagStream)).toList();

        return new PostResponse(
                post.getId(), post.getAuthorId(),
                communityQueryPolicy.resolveNickname(post.getAuthorId()),
                communityQueryPolicy.resolveProfileImageUrl(post.getAuthorId()),
                tags, post.getTitle(), post.getContent(),
                post.getCountryId(), countryName, post.getImageUrls(),
                0,
                likeCount, dislikeCount,
                (long) activeComments.size(), comments, post.getCreatedAt());
    }

    private List<CommentResponse> toCommentTree(List<Comment> comments) {
        return comments.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> new CommentResponse(
                        c.getCommentId(), c.getUserId(),
                        communityQueryPolicy.resolveNickname(c.getUserId()),
                        communityQueryPolicy.resolveProfileImageUrl(c.getUserId()),
                        c.getContent(), c.getCreatedAt(),
                        likeDislikeRepository.countLikes(TargetType.COMMENT, c.getCommentId()),
                        likeDislikeRepository.countDislikes(TargetType.COMMENT, c.getCommentId()),
                        comments.stream()
                                .filter(r -> c.getCommentId().equals(r.getParentId()))
                                .map(r -> new CommentResponse(
                                        r.getCommentId(), r.getUserId(),
                                        communityQueryPolicy.resolveNickname(r.getUserId()),
                                        communityQueryPolicy.resolveProfileImageUrl(r.getUserId()),
                                        r.getContent(), r.getCreatedAt(),
                                        likeDislikeRepository.countLikes(TargetType.COMMENT, r.getCommentId()),
                                        likeDislikeRepository.countDislikes(TargetType.COMMENT, r.getCommentId()),
                                        List.of()
                                )).toList()
                )).toList();
    }
}