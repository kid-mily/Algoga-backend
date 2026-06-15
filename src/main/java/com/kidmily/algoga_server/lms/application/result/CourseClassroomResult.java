package com.kidmily.algoga_server.lms.application.result;

import java.time.LocalDateTime;
import java.util.List;

public record CourseClassroomResult(
        Long courseId,
        String title,
        LocalDateTime accessExpiresAt,
        boolean quizAvailable,
        List<CourseClassroomChapterResult> chapters
) {
}
