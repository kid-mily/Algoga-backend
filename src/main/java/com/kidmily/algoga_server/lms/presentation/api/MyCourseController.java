package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.usecase.MyCourseUseCase;
import com.kidmily.algoga_server.lms.presentation.response.MyCourseResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
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

    private final MyCourseUseCase myCourseUseCase;

    @Operation(
            summary = "내 수강 강의 목록 조회",
            description = """
                    로그인한 사용자가 진도율을 기록한 강의 목록을 조회합니다.
                    결제 기능은 제외하고, learning_progresses 기록 기준으로 수강 내역을 구성합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<MyCourseResponse>>> getMyCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        List<MyCourseResponse> response = myCourseUseCase.getMyCourses(currentUserId)
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
}