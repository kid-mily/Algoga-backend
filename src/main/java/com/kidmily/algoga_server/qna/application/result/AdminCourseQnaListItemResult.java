package com.kidmily.algoga_server.qna.application.result;

import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.qna.domain.model.CourseQna;

import java.time.LocalDateTime;

public record AdminCourseQnaListItemResult(
        Long qnaId,
        Long courseId,
        String courseTitle,
        Long userId,
        String username,
        String name,
        String email,
        Long managerId,
        String title,
        String question,
        String answer,
        String status,
        LocalDateTime createdAt,
        LocalDateTime answeredAt
) {
    public static AdminCourseQnaListItemResult from(
            CourseQna qna,
            String courseTitle,
            UserProfilePort.UserProfile profile
    ) {
        return new AdminCourseQnaListItemResult(
                qna.getId(),
                qna.getCourseId(),
                courseTitle,
                qna.getUserId(),
                profile == null ? null : profile.username(),
                profile == null ? null : profile.name(),
                profile == null ? null : profile.email(),
                qna.getManagerId(),
                qna.getTitle(),
                qna.getQuestion(),
                qna.getAnswer(),
                qna.getStatus(),
                qna.getCreatedAt(),
                qna.getAnsweredAt()
        );
    }
}
