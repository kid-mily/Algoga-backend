package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.lms.presentation.response.CourseClassroomResponse;
import com.kidmily.algoga_server.lms.presentation.response.MyCourseResponse;
import com.kidmily.algoga_server.lms.presentation.support.CurrentUserIdResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "마이페이지 수강 내역", description = "마이페이지 수강 강의 조회 API")
@RestController
@RequestMapping("/api/v1/my/courses")
@RequiredArgsConstructor
public class MyCourseController {

    private final CourseUseCase courseUseCase;

    @Operation(
            summary = "내 수강 강의 목록 조회",
            description = """
                    로그인한 사용자가 진도율을 기록한 강의 목록을 조회합니다.
                    결제 기능은 제외하고, learning_progresses 기록 기준으로 수강 내역을 구성합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<MyCourseResponse>>> getMyCourses(
            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveNullable(userDetails);

        List<MyCourseResponse> response = courseUseCase.getMyCourses(currentUserId)
                .stream()
                .map(MyCourseResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MY_COURSES_FOUND",
                        "내 수강 강의 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "수강생 강의실 상세 조회",
            description = "수강 중인 강의의 챕터 영상과 진도, 잠금 상태 및 퀴즈 응시 가능 여부를 조회합니다."
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND", "NOT_ENROLLED"})
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseClassroomResponse>> getCourseClassroom(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveNullable(userDetails);
        var result = courseUseCase.getCourseClassroom(currentUserId, courseId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_CLASSROOM_FOUND",
                        "수강생 강의실 조회에 성공했습니다.",
                        CourseClassroomResponse.from(result)
                )
        );
    }
}
