package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.command.CreateCommentCommand;
import com.kidmily.algoga_server.community.application.command.DeleteCommentCommand;
import com.kidmily.algoga_server.community.application.command.UpdateCommentCommand;
import com.kidmily.algoga_server.community.application.policy.CommunityQueryPolicy;
import com.kidmily.algoga_server.community.application.policy.DeleteCommentPolicy;
import com.kidmily.algoga_server.community.application.port.UserPort;
import com.kidmily.algoga_server.community.application.usecase.CommentCommandUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.CommentException;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.notification.domain.event.CommentRepliedEvent;
import com.kidmily.algoga_server.notification.domain.event.PostCommentedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentCommandService implements CommentCommandUseCase {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final DeleteCommentPolicy deleteCommentPolicy;
    private final ApplicationEventPublisher eventPublisher;
    private final CommunityQueryPolicy communityQueryPolicy;

    // 댓글 작성
    @Override
    public Comment handle(CreateCommentCommand command) {
        log.info("[CommentCommandService] 댓글 작성 요청 수신 - postId: {}, userId: {}, parentId: {}",
                command.postId(), command.userId(), command.parentId());

        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        // 대댓글인 경우 부모 댓글 존재 여부 검증
        Long parentCommentAuthorId = null;
        if (command.parentId() != null) {
            Comment parentComment = commentRepository.findById(command.parentId())
                    .orElseThrow(() -> new CommentException(PostErrorCode.COMMENT_NOT_FOUND));

            Comment.validateDepth(parentComment.getParentId());

            parentCommentAuthorId = parentComment.getUserId();
        }

        Comment comment = Comment.create(
                command.postId(),
                command.userId(),
                command.parentId(),
                command.content()
        );

        Comment savedComment = commentRepository.save(comment);

        // 🌟 알림 이벤트 발행
        String commenterNickname = communityQueryPolicy.resolveNickname(command.userId());

        if (command.parentId() == null) {
            // 일반 댓글 → 게시글 작성자에게 알림 (본인이 본인 글에 댓글 단 경우 제외)
            if (!post.getAuthorId().equals(command.userId())) {
                eventPublisher.publishEvent(new PostCommentedEvent(
                        post.getAuthorId(),
                        command.userId(),
                        commenterNickname,
                        command.postId(),
                        command.content()
                ));
            }
        } else {
            // 대댓글 → 부모 댓글 작성자에게 알림 (본인이 본인 댓글에 대댓글 단 경우 제외)
            if (parentCommentAuthorId != null && !parentCommentAuthorId.equals(command.userId())) {
                eventPublisher.publishEvent(new CommentRepliedEvent(
                        parentCommentAuthorId,
                        command.userId(),
                        commenterNickname,
                        command.postId(),
                        command.parentId(),
                        command.content()
                ));
            }
        }

        log.info("[CommentCommandService] 댓글 작성 완료 - commentId: {}", savedComment.getCommentId());

        return savedComment;
    }

    // 댓글 수정
    @Override
    public Comment handle(UpdateCommentCommand command) {
        log.info("[CommentCommandService] 댓글 수정 요청 수신 - commentId: {}, userId: {}",
                command.commentId(), command.userId());

        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new CommentException(PostErrorCode.COMMENT_NOT_FOUND));

        comment.updateContent(command.userId(), command.content());
        Comment updatedComment = commentRepository.update(comment);

        log.info("[CommentCommandService] 댓글 수정 완료 - commentId: {}", updatedComment.getCommentId());
        return updatedComment;
    }

    // 댓글 삭제
    @Override
    public void handle(DeleteCommentCommand command) {
        log.info("[CommentCommandService] 댓글 삭제 요청 수신 - commentId: {}, userId: {}",
                command.commentId(), command.userId());

        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new CommentException(PostErrorCode.COMMENT_NOT_FOUND));

        deleteCommentPolicy.execute(comment, command.userId());

        log.info("[CommentCommandService] 댓글 삭제 완료 - commentId: {}", command.commentId());
    }
}