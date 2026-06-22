package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.CourseQna;

import java.time.LocalDateTime;

public record CourseQnaResult(
        Long qnaId,
        Long courseId,
        Long userId,
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

    public static CourseQnaResult from(CourseQna qna, String nickname) {
        return new CourseQnaResult(
                qna.getId(),
                qna.getCourseId(),
                qna.getUserId(),
                nickname,
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