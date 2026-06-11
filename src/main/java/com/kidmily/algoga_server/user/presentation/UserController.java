package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.application.UserService;
import com.kidmily.algoga_server.user.presentation.request.UpdatePasswordRequest;
import com.kidmily.algoga_server.user.presentation.request.UpdateProfileRequest;
import com.kidmily.algoga_server.user.presentation.request.VerifyPasswordRequest;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.UserProfileResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "회원 정보 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // 내 프로필 조회
    @Operation(summary = "내 프로필 상세 조회", description = "로그인한 유저의 전체 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse response = userService.getMyProfile(userDetails.getUsername());
        return ApiResponse.success("USER_PROFILE_SUCCESS", "프로필 조회를 성공했습니다.", response);
    }

    // 비밀번호 확인 (정보 수정 진입 전)
    @Operation(summary = "정보 수정 진입 전 비밀번호 확인", description = "사용자가 입력한 현재 비밀번호가 올바른지 검증합니다.")
    @PostMapping("/me/verify-password")
    public ApiResponse<Void> verifyPassword(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody VerifyPasswordRequest request) {
        userService.verifyPassword(userDetails.getUsername(), request);
        return ApiResponse.success("USER_VERIFY_PW_SUCCESS", "비밀번호가 확인되었습니다.");
    }

    // 프로필 정보 업데이트
    @Operation(summary = "내 프로필 정보 업데이트", description = "프로필 사진, 닉네임, 전화번호, 이메일을 수정합니다.")
    // 파일을 안전하게 처리하기 위해 consumes 속성 추가 및 @ModelAttribute 적용
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AuthTokenResponse> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute UpdateProfileRequest request) {

        AuthTokenResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ApiResponse.success("USER_UPDATE_SUCCESS", "프로필 정보가 수정되었습니다.", response);
    }

    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새로운 비밀번호로 변경합니다.")
    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request);
        return ApiResponse.success("USER_UPDATE_PW_SUCCESS", "비밀번호가 성공적으로 변경되었습니다.");
    }

    // 회원 탈퇴
    @Operation(summary = "회원 탈퇴", description = "계정을 Soft Delete 처리합니다.")
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.withdraw(userDetails.getUsername());
        return ApiResponse.success("USER_WITHDRAW_SUCCESS", "회원 탈퇴가 완료되었습니다.");
    }
}