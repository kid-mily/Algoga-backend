package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.policy.CommunityQueryPolicy;
import com.kidmily.algoga_server.community.application.usecase.PostQueryUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.port.ViewCountPort;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.LikeDislikeRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import com.kidmily.algoga_server.community.domain.model.TargetType;
import com.kidmily.algoga_server.community.presentation.api.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService implements PostQueryUseCase {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeDislikeRepository likeDislikeRepository;
    private static final int PAGE_SIZE = 10;
    private static final int POPULAR_COUNTRY_TAG_LIMIT = 5;
    private final CommunityQueryPolicy communityQueryPolicy;
    private final ViewCountPort viewCountPort;
    private final PostReadService postReadService;


    @Override
    public PostResponse getPost(Long postId) {
        log.info("[PostQueryService] 게시글 단건 조회 요청 - postId: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        // 1) 조회수는 항상 실시간 증가 (캐시 안 탐)
        viewCountPort.increment(postId);

        // 2) 본문/댓글/좋아요는 캐시에서 가져옴 (별도 빈 호출 → 프록시 적용)
        PostResponse cached = postReadService.getPostContentOnly(postId);

        // 3) DB 누적 조회수 + Redis 미반영분 합산
        int viewCount = post.getViewCount() + (int) viewCountPort.getCurrentCount(postId);

        return cached.withViewCount(viewCount);
    }


    @Override
    public List<PostTagType> getCategories() {
        return List.of(
                PostTagType.TRAVEL_REVIEW,
                PostTagType.TIP_INFO,
                PostTagType.QUESTION,
                PostTagType.COMPANION,
                PostTagType.FREE,
                PostTagType.LECTURE
        );
    }

    @Override
    public PostListResponse getPosts(Long lastPostId, List<PostTagType> categories, Long countryId) {
        log.info("[PostQueryService] 게시글 목록 조회 요청 - lastPostId: {}, categories: {}, countryId: {}",
                lastPostId, categories, countryId);

        List<Post> posts = postRepository.findPostsByCursor(lastPostId, PAGE_SIZE, categories, countryId);

        List<PostListItemResponse> items = posts.stream()
                .map(this::toListItem)
                .toList();

        boolean hasNext = items.size() == PAGE_SIZE;
        Long nextLastPostId = items.isEmpty() ? null : items.get(items.size() - 1).postId();

        return new PostListResponse(items, hasNext, nextLastPostId);
    }

    @Override
    public List<TagResponse> getPostFilterTags() {
        List<TagResponse> categoryTags = getCategories().stream()
                .map(TagResponse::fromCategory)
                .toList();

        List<TagResponse> countryTags = postRepository.findTopCountryTags(POPULAR_COUNTRY_TAG_LIMIT)
                .stream()
                .map(count -> {
                    String countryName = communityQueryPolicy.resolveCountryName(count.countryId());
                    return countryName != null ? TagResponse.fromCountry(count.countryId(), countryName) : null;
                })
                .filter(Objects::nonNull)
                .toList();

        return Stream.concat(categoryTags.stream(), countryTags.stream()).toList();
    }


    // 내가 쓴 글 목록 조회
    @Override
    public PostListResponse getMyPosts(Long userId, Long lastPostId, List<PostTagType> categories) {
        log.info("[PostQueryService] 내가 작성한 게시글 목록 조회 요청 - userId: {}, lastPostId: {}, categories: {}",
                userId, lastPostId, categories);

        // 도메인 포트(PostRepository) 호출
        List<Post> posts = postRepository.findMyPostsByCursor(userId, lastPostId, PAGE_SIZE, categories);

        List<PostListItemResponse> items = posts.stream()
                .map(this::toListItem)
                .toList();

        boolean hasNext = items.size() == PAGE_SIZE;
        Long nextLastPostId = items.isEmpty() ? null : items.get(items.size() - 1).postId();

        return new PostListResponse(items, hasNext, nextLastPostId);
    }

    private PostListItemResponse toListItem(Post post) {
        Long likeCount = likeDislikeRepository.countLikes(TargetType.POST, post.getId());
        Long dislikeCount = likeDislikeRepository.countDislikes(TargetType.POST, post.getId());
        Long commentCount = (long) commentRepository.findActiveCommentsByPostId(post.getId()).size();

        String nickname = communityQueryPolicy.resolveNickname(post.getAuthorId());
        String profileImageUrl = communityQueryPolicy.resolveProfileImageUrl(post.getAuthorId());
        String countryName = communityQueryPolicy.resolveCountryName(post.getCountryId());

        Stream<TagResponse> countryStream = countryName != null ?
                Stream.of(TagResponse.fromCountry(post.getCountryId(), countryName)) : Stream.empty();

        Stream<TagResponse> categoryStream = post.getCategory() != null ?
                Stream.of(TagResponse.fromCategory(post.getCategory())) : Stream.empty();
        Stream<TagResponse> freeTagStream = post.getFreeTags() != null ?
                post.getFreeTags().stream().map(TagResponse::fromFreeTag) : Stream.empty();
        List<TagResponse> tags = Stream.concat(countryStream, Stream.concat(categoryStream, freeTagStream)).toList();

        String thumbnailUrl = (post.getImageUrls() != null && !post.getImageUrls().isEmpty())
                ? post.getImageUrls().get(0)
                : null;

        return new PostListItemResponse(
                post.getId(),
                post.getAuthorId(),
                nickname,
                profileImageUrl,
                post.getCountryId(),
                countryName,        // ← "임시나라" → countryName으로 변경
                tags,
                post.getTitle(),
                post.getContent(),
                thumbnailUrl,
                likeCount,
                dislikeCount,
                commentCount,
                post.getViewCount(),
                post.getCreatedAt()
        );
    }

    private List<CommentResponse> toCommentTree(List<Comment> comments) {
        // 일반 댓글만 추출
        List<CommentResponse> roots = comments.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> new CommentResponse(
                        c.getCommentId(),
                        c.getUserId(),
                        communityQueryPolicy.resolveNickname(c.getUserId()),
                        communityQueryPolicy.resolveProfileImageUrl(c.getUserId()),
                        c.getContent(),
                        c.getCreatedAt(),
                        likeDislikeRepository.countLikes(TargetType.COMMENT, c.getCommentId()),
                        likeDislikeRepository.countDislikes(TargetType.COMMENT, c.getCommentId()),
                        // 해당 댓글의 대댓글 붙이기
                        comments.stream()
                                .filter(r -> c.getCommentId().equals(r.getParentId()))
                                .map(r -> new CommentResponse(
                                        r.getCommentId(),
                                        r.getUserId(),
                                        communityQueryPolicy.resolveNickname(r.getUserId()),
                                        communityQueryPolicy.resolveProfileImageUrl(r.getUserId()),
                                        r.getContent(),
                                        r.getCreatedAt(),
                                        likeDislikeRepository.countLikes(TargetType.COMMENT, r.getCommentId()),
                                        likeDislikeRepository.countDislikes(TargetType.COMMENT, r.getCommentId()),
                                        List.of()
                                ))
                                .toList()
                ))
                .toList();
        return roots;
    }

    // 관리자용 유저별 게시글 목록 조회 페이징 방식
    @Override
    public AdminPostListResponse getMyPostsByPage(Long userId, Integer index, List<PostTagType> categories) {
        int pageIndex = Math.max(0, index - 1);

        List<Post> posts = postRepository.findMyPostsByPage(userId, pageIndex, PAGE_SIZE, categories);
        long totalElements = postRepository.countMyPosts(userId, categories);
        int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);

        List<PostListItemResponse> items = posts.stream()
                .map(this::toListItem)
                .toList();

        return new AdminPostListResponse(items, totalElements, totalPages, index);
    }

    // 관리자용 유저 게시글 상세 조회 (조회수 증가 없음)
    @Override
    public PostResponse getPostForAdmin(Long postId) {
        log.info("[PostQueryService] 게시글 단건 조회 요청 (관리자) - postId: {}", postId);

        Post post = findPostById(postId);

        return buildPostResponse(post);
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
    }


    private PostResponse buildPostResponse(Post post) {
        Long likeCount = likeDislikeRepository.countLikes(TargetType.POST, post.getId());
        Long dislikeCount = likeDislikeRepository.countDislikes(TargetType.POST, post.getId());

        List<CommentResponse> comments = toCommentTree(
                commentRepository.findActiveCommentsByPostId(post.getId())
        );

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
                post.getId(),
                post.getAuthorId(),
                communityQueryPolicy.resolveNickname(post.getAuthorId()),
                communityQueryPolicy.resolveProfileImageUrl(post.getAuthorId()),
                tags,
                post.getTitle(),
                post.getContent(),
                post.getCountryId(),
                countryName,
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