package com.kidmily.algoga_server.community.domain.model;

import com.kidmily.algoga_server.community.exception.CommentException;
import com.kidmily.algoga_server.community.exception.PostErrorCode;

import java.time.LocalDateTime;

public class Comment {

    private final Long commentId;
    private final Long postId;
    private final Long userId;
    private final Long parentId;   // null이면 일반 댓글, 값 있으면 대댓글
    private String content;
    private boolean deleted;
    private final LocalDateTime createdAt;

    // 신규 생성용
    public static Comment create(Long postId, Long userId, Long parentId, String content) {
        validate(content);
        return new Comment(null, postId, userId, parentId, content, false, null);
    }

    public void updateContent(Long requesterId, String newContent) {
        validateOwnerForUpdate(requesterId);
        validate(newContent);
        this.content = newContent;
    }

    public void delete(Long requesterId) {
        validateOwnerForDelete(requesterId);
        this.deleted = true;
    }

    private void validateOwnerForUpdate(Long requesterId) {
        if (!this.userId.equals(requesterId)) {
            throw new CommentException(PostErrorCode.COMMENT_UPDATE_FORBIDDEN);
        }
    }

    private void validateOwnerForDelete(Long requesterId) {
        if (!this.userId.equals(requesterId)) {
            throw new CommentException(PostErrorCode.COMMENT_DELETE_FORBIDDEN);
        }
    }

    // DB 복원용
    public static Comment reconstitute(Long commentId, Long postId, Long userId,
                                       Long parentId, String content,
                                       boolean deleted, LocalDateTime createdAt) {
        return new Comment(commentId, postId, userId, parentId, content, deleted, createdAt);
    }

    private static void validate(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("댓글은 최대 500자입니다.");
        }
    }

    private Comment(Long commentId, Long postId, Long userId, Long parentId,
                    String content, boolean deleted, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.parentId = parentId;
        this.content = content;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }

    public Long getCommentId() { return commentId; }
    public Long getPostId() { return postId; }
    public Long getUserId() { return userId; }
    public Long getParentId() { return parentId; }
    public String getContent() { return content; }
    public boolean isDeleted() { return deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isReply() { return parentId != null; }

    // 대댓글의 대댓글 방지
    public static void validateDepth(Long parentId) {
        if (parentId != null) {
            throw new CommentException(PostErrorCode.COMMENT_DEPTH_EXCEEDED);
        }
    }

    // 부모 댓글이 해당 게시글 소속인지 검증
    public void validateBelongsToPost(Long postId) {
        if (!this.postId.equals(postId)) {
            throw new CommentException(PostErrorCode.COMMENT_NOT_FOUND);
        }
    }
}