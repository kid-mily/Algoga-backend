package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.application.UserService;
import com.kidmily.algoga_server.user.presentation.response.AdminUserListResponse;
import com.kidmily.algoga_server.user.presentation.response.SignupPathStatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Admin-Statistics", description = "관리자 전용 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final UserService userService;

    @Operation(summary = "유저 유입(가입) 경로 통계", description = "어떤 경로(검색, 지인추천 등)를 통해 유저들이 가입했는지 비율과 수치를 집계합니다.")
    @GetMapping("/statistics/signup-paths")
    public ApiResponse<List<SignupPathStatResponse>> getSignupPathStats() {

        List<SignupPathStatResponse> stats = userService.getSignupPathStats();

        return ApiResponse.success("STAT_SIGNUP_PATH_SUCCESS", "유저 가입 경로 통계 조회를 성공했습니다.", stats);
    }

    @Operation(summary = "유저 리스트 전체 조회 (관리자용)", description = "CS 매니저가 유저 리스트와 통계(친구/게시글 수)를 페이징하여 조회합니다.")
    @GetMapping
    public ApiResponse<Page<AdminUserListResponse>> getUserList(
            @PageableDefault(size = 10) Pageable pageable) { // 한 페이지에 10명씩 가져옴

        Page<AdminUserListResponse> response = userService.getAdminUserList(pageable);

        return ApiResponse.success("ADMIN_USER_LIST_SUCCESS", "유저 목록 조회를 성공했습니다.", response);
    }

    @Operation(summary = "유저 상세 정보 및 친구 탭 조회 (관리자용)", description = "특정 유저의 상세 기본 정보, 실시간 로그인 상태, 친구 목록 탭에 들어갈 데이터를 한 번에 조회합니다.")
    @GetMapping("/{userId}")
    public ApiResponse<com.kidmily.algoga_server.user.presentation.response.AdminUserDetailResponse> getUserDetail(
            @org.springframework.web.bind.annotation.PathVariable("userId") Long userId) {

        com.kidmily.algoga_server.user.presentation.response.AdminUserDetailResponse response = userService.getAdminUserDetail(userId);

        return ApiResponse.success("ADMIN_USER_DETAIL_SUCCESS", "유저 상세 정보 조회를 성공했습니다.", response);
    }
}