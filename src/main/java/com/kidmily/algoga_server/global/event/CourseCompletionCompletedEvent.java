package com.kidmily.algoga_server.global.event;

import java.time.LocalDateTime;

public record CourseCompletionCompletedEvent(
        Long userId,
        Long courseId,
        Long completionId,
        LocalDateTime completedAt
) {
}