package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseQnaResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Course Q&A response")
public record CourseQnaResponse(
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
    public static CourseQnaResponse from(CourseQnaResult qna) {
        return new CourseQnaResponse(
                qna.qnaId(),
                qna.courseId(),
                qna.userId(),
                qna.managerId(),
                qna.title(),
                qna.question(),
                qna.answer(),
                qna.status(),
                qna.createdAt(),
                qna.answeredAt()
        );
    }
}