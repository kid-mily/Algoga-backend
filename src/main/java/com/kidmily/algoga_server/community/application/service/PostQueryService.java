package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.usecase.PostQueryUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.model.Post;
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

    @Override
    @Transactional
    public PostResponse getPost(Long postId) {
        log.info("[PostQueryService] 게시글 단건 조회 요청 - postId: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));


        // 조회수 증가
        post.increaseViewCount();
        postRepository.update(post);

        // 좋아요/싫어요 수 카운트
        Long likeCount = likeDislikeRepository.countLikes(TargetType.POST, postId);
        Long dislikeCount = likeDislikeRepository.countDislikes(TargetType.POST, postId);


//        List<CreateCommentResponse> comments = commentRepository
//                .findActiveCommentsByPostId(postId)
//                .stream()
//                .map(c -> new CreateCommentResponse(c.getCommentId(), c.getUserId(), c.getContent(),  c.getParentId(), c.getCreatedAt()))
//                .toList();


        List<CommentResponse> comments = toCommentTree(
                commentRepository.findActiveCommentsByPostId(postId)
        );

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
    public PostListResponse getPosts(Long lastPostId, List<PostTagType> categories) {
        log.info("[PostQueryService] 게시글 목록 조회 요청 - lastPostId: {}, categories: {}",
                lastPostId, categories);

        List<Post> posts = postRepository.findPostsByCursor(lastPostId, PAGE_SIZE, categories);

        List<PostListItemResponse> items = posts.stream()
                .map(this::toListItem)
                .toList();

        boolean hasNext = items.size() == PAGE_SIZE;
        Long nextLastPostId = items.isEmpty() ? null : items.get(items.size() - 1).postId();

        return new PostListResponse(items, hasNext, nextLastPostId);
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

        Stream<TagResponse> categoryStream = post.getCategory() != null ?
                Stream.of(TagResponse.fromCategory(post.getCategory())) : Stream.empty();
        Stream<TagResponse> freeTagStream = post.getFreeTags() != null ?
                post.getFreeTags().stream().map(TagResponse::fromFreeTag) : Stream.empty();
        List<TagResponse> tags = Stream.concat(categoryStream, freeTagStream).toList();

        String thumbnailUrl = (post.getImageUrls() != null && !post.getImageUrls().isEmpty())
                ? post.getImageUrls().get(0)
                : null;

        return new PostListItemResponse(
                post.getId(),
                post.getAuthorId(),
                "임시닉네임",  // TODO: User 도메인 추가 후 교체
                null, // TODO: User 도메인 추가 후 교체, 프로필 url
                post.getCountryId(), // TODO: 나라 도메인 추가 후 교체
                "임시나라",  // TODO: 나라 도메인 추가 후 교체
                tags,
                post.getTitle(),
                post.getContent(),
                thumbnailUrl,
                likeCount,
                dislikeCount,
                commentCount,
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
                        c.getContent(),
                        c.getCreatedAt(),
                        // 해당 댓글의 대댓글 붙이기
                        comments.stream()
                                .filter(r -> c.getCommentId().equals(r.getParentId()))
                                .map(r -> new CommentResponse(
                                        r.getCommentId(),
                                        r.getUserId(),
                                        r.getContent(),
                                        r.getCreatedAt(),
                                        List.of()
                                ))
                                .toList()
                ))
                .toList();
        return roots;
    }


}