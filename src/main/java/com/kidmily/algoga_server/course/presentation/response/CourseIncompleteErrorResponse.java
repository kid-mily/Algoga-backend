package com.kidmily.algoga_server.course.presentation.response;

import com.kidmily.algoga_server.course.application.result.CoursePublishRequirementResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "강의 공개 조건 미충족 에러 응답")
public record CourseIncompleteErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String traceId,
        CoursePublishRequirementResult data
) {
}
