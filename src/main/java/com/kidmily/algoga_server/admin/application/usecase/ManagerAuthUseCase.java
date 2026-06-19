package com.kidmily.algoga_server.admin.application.usecase;

import com.kidmily.algoga_server.admin.application.command.LoginManagerCommand;
import com.kidmily.algoga_server.admin.presentation.api.response.AdminAuthTokenResponse;

public interface ManagerAuthUseCase {
    AdminAuthTokenResponse login(LoginManagerCommand command);

    // 로그아웃 (Redis 토큰 삭제)
    void logout(String loginId);

    // 🌟 변경: 로그인과 동일하게 Role 정보를 함께 넘겨주기 위해 반환 타입 수정
    AdminAuthTokenResponse refreshAccessToken(String loginId, String refreshToken);
}