package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.application.port.UserProfilePort;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;

import java.time.LocalDateTime;

public record CourseQnaResult(
        Long qnaId,
        Long courseId,
        Long userId,
        String username,
        String name,
        String email,
        String nickname,
        Long managerId,
        String title,
        String question,
        String answer,
        String status,
        LocalDateTime createdAt,
        LocalDateTime answeredAt
) {
    public static CourseQnaResult from(CourseQna qna) {
        return from(qna, null);
    }

    public static CourseQnaResult from(CourseQna qna, UserProfilePort.UserProfile profile) {
        return new CourseQnaResult(
                qna.getId(),
                qna.getCourseId(),
                qna.getUserId(),
                profile == null ? null : profile.username(),
                profile == null ? null : profile.name(),
                profile == null ? null : profile.email(),
                profile == null ? null : profile.nickname(),
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