package com.kidmily.algoga_server.user.presentation.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "프로필 수정 요청")
public record UpdateProfileRequest(
        @Schema(description = "닉네임", example = "알고가조아")
        String nickname,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        // MultipartFile에는 @Parameter를 붙여서 스웨거가 파일로 인식하게 합니다.
        @Parameter(description = "프로필 이미지 파일")
        MultipartFile profileImage,

        @Schema(description = "이메일", example = "new@algoga.com")
        String email
) {}