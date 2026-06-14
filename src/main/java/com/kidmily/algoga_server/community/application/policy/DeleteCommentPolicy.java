package com.kidmily.algoga_server.community.application.policy;

import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteCommentPolicy {

    private final CommentRepository commentRepository;

    public void execute(Comment comment, Long requesterId) {
        comment.delete(requesterId);
        commentRepository.delete(comment);

        if (!comment.isReply()) {
            commentRepository.findActiveRepliesByParentId(comment.getCommentId())
                    .forEach(reply -> {
                        reply.delete(requesterId);
                        commentRepository.delete(reply);
                    });
        }
    }

    public void executeByAdmin(Comment comment) {
        comment.deleteByAdmin();
        commentRepository.delete(comment);

        if (!comment.isReply()) {
            commentRepository.findActiveRepliesByParentId(comment.getCommentId())
                    .forEach(reply -> {
                        reply.deleteByAdmin();
                        commentRepository.delete(reply);
                    });
        }
    }
}