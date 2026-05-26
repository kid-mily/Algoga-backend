package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.command.CompleteCourseCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseCompletionUseCase;
import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.response.CourseCompletionResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "강의 이수", description = "사용자 강의 이수 완료 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/complete")
@RequiredArgsConstructor
public class CourseCompletionController {

    private final CourseCompletionUseCase courseCompletionUseCase;

    @Operation(
            summary = "강의 이수 완료 처리",
            description = """
                    사용자가 강의의 모든 챕터를 완료하고 퀴즈를 제출한 경우 강의 이수 완료 처리를 합니다.
                    이미 이수 완료한 강의는 중복 이수 처리할 수 없습니다.
                    """
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QUIZ_LOCKED",
            "QUIZ_NOT_SUBMITTED",
            "COURSE_ALREADY_COMPLETED"
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CourseCompletionResponse>> completeCourse(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        CompleteCourseCommand command = new CompleteCourseCommand(
                currentUserId,
                courseId
        );

        CourseCompletion courseCompletion = courseCompletionUseCase.completeCourse(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_COMPLETED",
                        "강의 이수 완료 처리에 성공했습니다.",
                        CourseCompletionResponse.from(courseCompletion)
                ));
    }
}