package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.lms.application.command.UpdateLearningProgressCommand;
import com.kidmily.algoga_server.lms.application.usecase.LearningProgressUseCase;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.request.UpdateLearningProgressRequest;
import com.kidmily.algoga_server.lms.presentation.response.LearningProgressResponse;
import com.kidmily.algoga_server.lms.presentation.support.CurrentUserIdResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "강의 수강", description = "사용자 강의 수강 및 진도율 관리 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/chapters/{chapterId}/progress")
@RequiredArgsConstructor
public class LearningProgressController {

    private final LearningProgressUseCase learningProgressUseCase;

    @Operation(
            summary = "챕터 진도율 업데이트",
            description = """
                    사용자의 챕터 영상 시청 시간을 기준으로 진도율을 업데이트합니다.
                    기존 최대 시청 시간보다 작은 값이 들어와도 진도율은 감소하지 않습니다.
                    진도율이 100%에 도달하면 챕터 완료 상태로 처리합니다.
                    """
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "CHAPTER_NOT_FOUND",
            "INVALID_PROGRESS",
            "CHAPTER_LOCKED"
    })
    @PostMapping
    public ResponseEntity<ApiResponse<LearningProgressResponse>> updateProgress(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "챕터 ID", example = "6")
            @PathVariable Long chapterId,

            @Valid @RequestBody UpdateLearningProgressRequest request,

            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveLoginRequired(userDetails);

        UpdateLearningProgressCommand command = new UpdateLearningProgressCommand(
                currentUserId,
                courseId,
                chapterId,
                request.watchedSeconds()
        );

        var learningProgress = learningProgressUseCase.updateProgress(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "LEARNING_PROGRESS_UPDATED",
                        "챕터 진도율 업데이트에 성공했습니다.",
                        LearningProgressResponse.from(learningProgress)
                )
        );
    }
}
