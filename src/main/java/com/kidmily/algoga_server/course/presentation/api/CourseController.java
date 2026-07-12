package com.kidmily.algoga_server.course.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.course.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.course.presentation.response.CourseListResponse;
import com.kidmily.algoga_server.lms.presentation.support.CurrentUserIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseUseCase courseUseCase;

    @GetMapping("/countries/{countryId}")
    public ResponseEntity<ApiResponse<List<CourseListResponse>>> getCoursesByCountry(
            @PathVariable Long countryId,
            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveNullable(userDetails);

        List<CourseListResponse> response = courseUseCase.getPublishedCoursesByCountry(countryId)
                .stream()
                .map(course -> CourseListResponse.from(
                        course,
                        courseUseCase.isEnrolled(currentUserId, course.courseId()),
                        courseUseCase.isPaid(currentUserId, course.courseId())
                ))
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COUNTRY_COURSES_FOUND",
                        "국가별 강의 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseListResponse>> getCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveNullable(userDetails);
        var course = courseUseCase.getPublishedCourse(courseId);

        CourseListResponse response = CourseListResponse.from(
                course,
                courseUseCase.isEnrolled(currentUserId, course.courseId()),
                courseUseCase.isPaid(currentUserId, course.courseId())
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_FOUND",
                        "강의 상세 조회에 성공했습니다.",
                        response
                )
        );
    }

}
