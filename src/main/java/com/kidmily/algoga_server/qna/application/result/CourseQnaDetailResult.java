package com.kidmily.algoga_server.qna.application.result;

import java.time.LocalDateTime;
import java.util.List;

public record CourseQnaDetailResult(
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
        LocalDateTime answeredAt,
        List<CourseQnaCommentResult> comments
) {
}