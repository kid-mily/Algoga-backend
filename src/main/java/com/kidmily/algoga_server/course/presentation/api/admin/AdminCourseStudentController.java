package com.kidmily.algoga_server.course.presentation.api.admin;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.course.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.course.presentation.response.CourseStudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Course Student", description = "콘텐츠 매니저 강의 수강생 관리 API")
@RestController
@RequestMapping("/api/v1/admin/courses/{courseId}/students")
@RequiredArgsConstructor
public class AdminCourseStudentController {

    private final CourseUseCase courseUseCase;

    @Operation(
            summary = "강의 수강생 목록 조회",
            description = """
                    특정 강의에 진도율 기록이 있는 사용자 목록을 조회합니다.
                    결제 기능은 제외하고 learning_progresses 기록 기준으로 수강생을 구성합니다.
                    """
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseStudentResponse>>> getCourseStudents(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId
    ) {
        List<CourseStudentResponse> response = courseUseCase.getCourseStudents(courseId)
                .stream()
                .map(CourseStudentResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_STUDENTS_FOUND",
                        "강의 수강생 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}
