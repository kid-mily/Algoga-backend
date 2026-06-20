package com.kidmily.algoga_server.benefit.presentation.api.admin;

import com.kidmily.algoga_server.benefit.application.command.RetryCourseRewardFailureCommand;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardFailureUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CourseRewardFailureStatus;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.presentation.response.CourseRewardFailureResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Course Reward Failure", description = "관리자 강의 보상 실패 관리 API")
@RestController
@RequestMapping("/api/v1/admin/course-reward-failures")
@RequiredArgsConstructor
public class AdminCourseRewardFailureController {

    private final CourseRewardFailureUseCase courseRewardFailureUseCase;

    @Operation(
            summary = "강의 보상 실패 목록 조회",
            description = "강의 수료 후 자동 보상 지급에 실패한 기록을 상태별로 조회합니다."
    )
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseRewardFailureResponse>>> getRewardFailures(
            @Parameter(description = "보상 실패 상태", example = "PENDING")
            @RequestParam(defaultValue = "PENDING") CourseRewardFailureStatus status
    ) {
        List<CourseRewardFailureResponse> response = courseRewardFailureUseCase.getFailures(status)
                .stream()
                .map(CourseRewardFailureResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_REWARD_FAILURES_FOUND",
                        "강의 보상 실패 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "강의 보상 실패 재시도",
            description = "강의 수료 후 자동 보상 지급에 실패한 건을 다시 지급 시도합니다."
    )
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
            "COURSE_REWARD_FAILURE_NOT_FOUND",
            "COURSE_REWARD_FAILURE_ALREADY_RESOLVED",
            "COURSE_REWARD_ALREADY_GRANTED"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/{failureId}/retry")
    public ResponseEntity<ApiResponse<CourseRewardFailureResponse>> retryRewardFailure(
            @Parameter(description = "보상 실패 기록 ID", example = "1")
            @PathVariable Long failureId
    ) {
        CourseRewardFailureResponse response = CourseRewardFailureResponse.from(
                courseRewardFailureUseCase.retryFailure(
                        new RetryCourseRewardFailureCommand(failureId)
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_REWARD_FAILURE_RETRIED",
                        "강의 보상 실패 건 재시도에 성공했습니다.",
                        response
                )
        );
    }
}