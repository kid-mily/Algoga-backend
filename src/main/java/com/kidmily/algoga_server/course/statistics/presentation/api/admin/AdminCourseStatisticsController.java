package com.kidmily.algoga_server.course.statistics.presentation.api.admin;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.course.statistics.application.usecase.CourseStatisticsUseCase;
import com.kidmily.algoga_server.course.statistics.presentation.response.CourseEnrollmentStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/statistics/courses")
@RequiredArgsConstructor
public class AdminCourseStatisticsController {

    private final CourseStatisticsUseCase courseStatisticsUseCase;

    @GetMapping("/enrollment")
    @PreAuthorize("hasAnyAuthority('STATISTICS_MANAGER', 'ROLE_STATISTICS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CourseEnrollmentStatisticsResponse>> getCourseEnrollmentStatistics(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var result = courseStatisticsUseCase.getCourseEnrollmentStatistics(keyword, page, size);

        return ResponseEntity.ok(ApiResponse.success(
                "COURSE_ENROLLMENT_STATISTICS_FOUND",
                "수강률 통계 조회에 성공했습니다.",
                CourseEnrollmentStatisticsResponse.from(result)
        ));
    }
}