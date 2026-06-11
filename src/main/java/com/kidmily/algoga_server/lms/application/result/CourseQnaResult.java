package com.kidmily.algoga_server.lms.application.result;

import com.kidmily.algoga_server.lms.domain.model.CourseQna;

import java.time.LocalDateTime;

public record CourseQnaResult(
        Long qnaId,
        Long courseId,
        Long userId,
        Long managerId,
        String title,
        String question,
        String answer,
        String status,
        LocalDateTime createdAt,
        LocalDateTime answeredAt
) {
    public static CourseQnaResult from(CourseQna qna) {
        return new CourseQnaResult(
                qna.getId(),
                qna.getCourseId(),
                qna.getUserId(),
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