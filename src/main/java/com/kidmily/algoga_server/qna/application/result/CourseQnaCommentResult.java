package com.kidmily.algoga_server.qna.application.result;

import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.qna.domain.model.CourseQnaComment;

import java.time.LocalDateTime;

public record CourseQnaCommentResult(
        Long commentId,
        Long qnaId,
        Long parentCommentId,
        Long userId,
        String username,
        String name,
        String email,
        Long managerId,
        String writerType,
        String nickname,
        String content,
        LocalDateTime createdAt
) {
    public static CourseQnaCommentResult from(CourseQnaComment comment) {
        return from(comment, null);
    }

    public static CourseQnaCommentResult from(
            CourseQnaComment comment,
            UserProfilePort.UserProfile profile
    ) {
        return new CourseQnaCommentResult(
                comment.getId(),
                comment.getQnaId(),
                comment.getParentCommentId(),
                comment.getUserId(),
                profile == null ? null : profile.username(),
                profile == null ? null : profile.name(),
                profile == null ? null : profile.email(),
                comment.getManagerId(),
                comment.getWriterType(),
                profile == null ? null : profile.nickname(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}