package com.kidmily.algoga_server.admin.application.service;

import com.kidmily.algoga_server.admin.application.command.LoginManagerCommand;
import com.kidmily.algoga_server.admin.application.usecase.ManagerAuthUseCase;
import com.kidmily.algoga_server.admin.domain.model.Manager;
import com.kidmily.algoga_server.admin.domain.repository.ManagerRepository;
import com.kidmily.algoga_server.admin.exception.ManagerErrorCode;
import com.kidmily.algoga_server.admin.exception.ManagerException;
import com.kidmily.algoga_server.admin.presentation.api.response.AdminAuthTokenResponse;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerAuthService implements ManagerAuthUseCase {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalJwtProvider globalJwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public AdminAuthTokenResponse login(LoginManagerCommand command) {

        Manager manager = managerRepository.findByLoginId(command.loginId())
                .orElseThrow(() -> new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND));

        if (manager.isDeleted()) {
            throw new ManagerException(ManagerErrorCode.DELETED_MANAGER);
        }

        if (!passwordEncoder.matches(command.password(), manager.getPassword())) {
            throw new ManagerException(ManagerErrorCode.INVALID_PASSWORD);
        }

        String roleName = "ROLE_" + manager.getRole().name();

        String accessToken = globalJwtProvider.createAdminAccessToken(manager.getId(), manager.getLoginId(), roleName);
        String refreshToken = globalJwtProvider.createAdminRefreshToken(manager.getLoginId());

        // Redis에 Refresh Token 저장 (7일)
        redisTemplate.opsForValue().set(
                "ADMIN_RT:" + manager.getLoginId(),
                refreshToken,
                604800000,
                TimeUnit.MILLISECONDS
        );

        return new AdminAuthTokenResponse(accessToken, refreshToken, manager.getRole());
    }

    @Override
    public void logout(String loginId) {
        redisTemplate.delete("ADMIN_RT:" + loginId);
    }

    // 🌟 변경: 토큰 재발급 시에도 매니저 정보를 조회해 진짜 권한(Role)을 함께 반환함
    @Override
    public AdminAuthTokenResponse refreshAccessToken(String loginId, String refreshToken) {
        // Redis 토큰 검증
        String savedRefreshToken = redisTemplate.opsForValue().get("ADMIN_RT:" + loginId);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new ManagerException(ManagerErrorCode.ACCESS_DENIED);
        }

        // 매니저 권한 조회를 위해 DB 탐색
        Manager manager = managerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND));

        String roleName = "ROLE_" + manager.getRole().name();

        // 새로운 Access Token 발급
        String newAccessToken = globalJwtProvider.createAdminAccessToken(manager.getId(), manager.getLoginId(), roleName);

        // 쿠키용 토큰과 응답 바디용 Role을 세트로 묶어서 반환
        return new AdminAuthTokenResponse(newAccessToken, refreshToken, manager.getRole());
    }

    @PostConstruct
    public void printMyPasswordHash() {
        System.out.println("\n=======================================");
        System.out.println("🔥 내 서버용 완벽한 해시값: " + passwordEncoder.encode("password123!"));
        System.out.println("=======================================\n");
    }
}