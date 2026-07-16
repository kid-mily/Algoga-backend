package com.kidmily.algoga_server.user.presentation;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.user.application.UserService;
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.presentation.request.UpdatePasswordRequest;
import com.kidmily.algoga_server.user.presentation.request.UpdateProfileRequest;
import com.kidmily.algoga_server.user.presentation.request.VerifyEmailCodeRequest;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.UserProfileResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "회원 정보 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final GlobalJwtProvider globalJwtProvider;

    // 내 프로필 조회
    @Operation(summary = "내 프로필 상세 조회", description = "로그인한 유저의 전체 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse response = userService.getMyProfile(userDetails.getUsername());
        return ApiResponse.success("USER_PROFILE_SUCCESS", "프로필 조회를 성공했습니다.", response);
    }

    @Operation(
            summary = "정보 수정 진입 전 이메일 인증코드 발송",
            description = "사용자 본인 확인을 위해 가입된 이메일로 6자리 인증코드를 발송합니다."
    )
    @ApiErrorCodeExample(domain = UserErrorCode.class, value = {"NOT_FOUND_USER"})
    @PostMapping("/me/email/send-code")
    public ApiResponse<Void> sendMyPageAuthCode(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.sendMyPageVerificationCode(userDetails.getUsername());

        return ApiResponse.success("MYPAGE_AUTH_CODE_SENT", "인증번호가 발송되었습니다.");
    }

    @Operation(
            summary = "정보 수정 진입 전 이메일 인증코드 검증",
            description = "발송된 이메일 인증코드가 올바른지 검증하여 정보 수정 권한을 부여합니다."
    )
    @ApiErrorCodeExample(domain = AuthErrorCode.class, value = {"EMAIL_AUTH_CODE_MISMATCH"})
    @PostMapping("/me/email/verify")
    public ApiResponse<Void> verifyMyPageAuthCode(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody VerifyEmailCodeRequest request) {

        userService.verifyMyPageEmailCode(userDetails.getUsername(), request.code());

        return ApiResponse.success("MYPAGE_AUTH_VERIFIED", "본인 인증이 완료되었습니다.");
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
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        userService.withdraw(userDetails.getUsername());

        // 🌟 로그인/로그아웃과 동일한 domain 속성으로 쿠키를 지워야 실제로 삭제됨 (domain 불일치 시 삭제 안 되는 문제 방지)
        response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.deleteCookie("accessToken").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, globalJwtProvider.deleteCookie("refreshToken").toString());

        return ApiResponse.success("USER_WITHDRAW_SUCCESS", "회원 탈퇴가 완료되었습니다.");
    }
}