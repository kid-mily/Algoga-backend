package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.application.UserService;
import com.kidmily.algoga_server.user.presentation.response.SignupPathStatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin-Statistics", description = "관리자 전용 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics/users")
public class UserAdminController {

    private final UserService userService;

    @Operation(summary = "유저 유입(가입) 경로 통계", description = "어떤 경로(검색, 지인추천 등)를 통해 유저들이 가입했는지 비율과 수치를 집계합니다.")
    @GetMapping("/signup-paths")
    public ApiResponse<List<SignupPathStatResponse>> getSignupPathStats() {

        List<SignupPathStatResponse> stats = userService.getSignupPathStats();

        return ApiResponse.success("STAT_SIGNUP_PATH_SUCCESS", "유저 가입 경로 통계 조회를 성공했습니다.", stats);
    }
}