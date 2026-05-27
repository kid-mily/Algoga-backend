package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.presentation.response.CourseListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseUseCase courseUseCase;

    @GetMapping("/countries/{countryId}")
    public ResponseEntity<ApiResponse<List<CourseListResponse>>> getCoursesByCountry(
            @PathVariable Long countryId
    ) {
        List<CourseListResponse> response = courseUseCase.getPublishedCoursesByCountry(countryId)
                .stream()
                .map(CourseListResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COUNTRY_COURSES_FOUND",
                        "국가별 강의 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}