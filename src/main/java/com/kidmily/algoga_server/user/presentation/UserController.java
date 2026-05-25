package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.application.UserService;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "회원 정보 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 임시 조회 (토큰 테스트용)")
    @GetMapping("/me")
    public ApiResponse<String> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 토큰이 정상적으로 들어왔다면, 시큐리티가 userDetails에 유저 정보를 담아줍니다!
        String userEmail = userDetails.getUsername();

        return ApiResponse.success(
                "USER_INFO_SUCCESS",
                "인증 통과 완료! 접속한 유저: " + userEmail,
                userEmail
        );
    }

    @Operation(summary = "회원 탈퇴", description = "계정을 Soft Delete 처리합니다.")
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.withdraw(userDetails.getUsername());
        return ApiResponse.success("USER_WITHDRAW_SUCCESS", "회원 탈퇴가 완료되었습니다.");
    }
}