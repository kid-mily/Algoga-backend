package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.usecase.CommentQueryUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.CommentException;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.community.presentation.api.response.AdminCommentListItemResponse;
import com.kidmily.algoga_server.community.presentation.api.response.AdminCommentListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentQueryService implements CommentQueryUseCase {

    private static final int PAGE_SIZE = 10;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    public AdminCommentListResponse getMyCommentsByPage(Long userId, Integer index) {
        int pageIndex = Math.max(0, index - 1);

        List<Comment> comments = commentRepository.findMyCommentsByPage(userId, pageIndex, PAGE_SIZE);
        long totalElements = commentRepository.countMyComments(userId);
        int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);

        List<AdminCommentListItemResponse> items = comments.stream()
                .map(this::toListItem)
                .toList();

        return new AdminCommentListResponse(items, totalElements, totalPages, index);
    }

    @Override
    public AdminCommentListItemResponse getCommentForAdmin(Long commentId) {  // 반환 타입 변경
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(PostErrorCode.COMMENT_NOT_FOUND));

        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        return new AdminCommentListItemResponse(
                comment.getCommentId(),
                post.getId(),
                post.getTitle(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

// toListItem()도 동일하게 AdminCommentListItemResponse 반환 (기존과 동일)

    private AdminCommentListItemResponse toListItem(Comment comment) {
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        return new AdminCommentListItemResponse(
                comment.getCommentId(),
                post.getId(),
                post.getTitle(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countMyCommentsForUsers(List<Long> userIds) {
        return commentRepository.countMyCommentsForUsers(userIds);
    }



}