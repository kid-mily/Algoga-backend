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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 로그인은 조회만 하므로 readOnly 적용하여 성능 최적화
public class ManagerAuthService implements ManagerAuthUseCase {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalJwtProvider globalJwtProvider; // 앞서 만든 어드민 전용 JWT 프로바이더

    @Override
    public AdminAuthTokenResponse login(LoginManagerCommand command) {

        // 1. 아이디로 매니저 조회
        Manager manager = managerRepository.findByLoginId(command.loginId())
                .orElseThrow(() -> new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND));

        // 2. 삭제(Soft Delete)된 계정인지 확인
        if (manager.isDeleted()) {
            throw new ManagerException(ManagerErrorCode.DELETED_MANAGER);
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(command.password(), manager.getPassword())) {
            throw new ManagerException(ManagerErrorCode.INVALID_PASSWORD);
        }

        // 4. JWT 토큰 발급 (시큐리티 표준인 'ROLE_' 접두사 추가)
        String roleName = "ROLE_" + manager.getRole().name();

        // 주의: AdminJwtProvider에는 PK(manager.getId())를 함께 넣도록 앞서 구현해 두었습니다.
        String accessToken = globalJwtProvider.createAdminAccessToken(manager.getId(), manager.getLoginId(), roleName);
        String refreshToken = globalJwtProvider.createAdminRefreshToken(manager.getLoginId());

        return new AdminAuthTokenResponse(accessToken, refreshToken, manager.getRole());
    }

    @PostConstruct
    public void printMyPasswordHash() {
        System.out.println("\n=======================================");
        System.out.println("🔥 내 서버용 완벽한 해시값: " + passwordEncoder.encode("password123!"));
        System.out.println("=======================================\n");
    }
}