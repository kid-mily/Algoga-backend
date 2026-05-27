package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import com.kidmily.algoga_server.user.presentation.request.UpdatePasswordRequest;
import com.kidmily.algoga_server.user.presentation.request.UpdateProfileRequest;
import com.kidmily.algoga_server.user.presentation.request.VerifyPasswordRequest;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.UserProfileResponse;
import com.kidmily.algoga_server.user.settings.JwtProvider;
import com.kidmily.algoga_server.user.settings.UserStorageSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 🌟 S3 스토리지 공통 포트 및 유저 세팅 인프라 빈 주입
    private final FileStoragePort fileStoragePort;
    private final UserStorageSettings storageSettings;

    // 1. 내 프로필 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
        return UserProfileResponse.from(user);
    }

    // 2. 정보 수정 진입 전 비밀번호 검증
    @Transactional(readOnly = true)
    public void verifyPassword(String email, VerifyPasswordRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD); // "비밀번호가 일치하지 않습니다" 에러
        }
    }

    // 3. 내 프로필 수정
    @Transactional
    public AuthTokenResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        String newEmail = (request.email() != null) ? request.email() : user.getEmail();

        // 이메일 변경 시 중복 체크
        if (!newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new UserException(UserErrorCode.ALREADY_EXISTS_EMAIL);
            }
        }

        // 🌟 S3 이미지 스토리지 업로드 분기 처리 로직
        String targetImageUrl = user.getProfileImageUrl();
        MultipartFile imageFile = request.profileImage();

        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 등록된 이미지가 존재하면 S3 버킷에서 선제적으로 안전하게 무효화(삭제)
            if (targetImageUrl != null && !targetImageUrl.isBlank()) {
                fileStoragePort.deleteFile(storageSettings.getBucketName(), targetImageUrl);
            }
            // 새로운 멀티파트 파일을 지정 버킷 및 디렉토리에 전송 후 엔드포인트 URL 추출
            targetImageUrl = fileStoragePort.uploadFile(
                    imageFile,
                    storageSettings.getBucketName(),
                    storageSettings.getDirectory()
            );
        }

        // Entity 내부 값 업데이트 (Dirty Checking 유도)
        user.updateProfile(request.nickname(), request.phone(), targetImageUrl, newEmail);

        // 새로운 식별정보를 기반으로 신규 세션 토큰 재발급
        String newToken = jwtProvider.createAccessToken(newEmail);

        return new AuthTokenResponse(
                newToken,
                null,
                user.getRequiresPasswordChange()
        );
    }

    // 4. 비밀번호 변경
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        // 기존 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 암호화 후 변경
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    // 1. 회원 탈퇴 (Soft Delete)
    public void withdraw(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.DELETED_USER);
        }

        user.withdraw(); // User 엔티티의 isDeleted 상태를 true로 변경

        // @Transactional 안에서 엔티티 값이 변경되면 JPA가 알아서 DB에 UPDATE 쿼리를 날립니다! (더티 체킹)
        log.info("회원 탈퇴 처리 완료 [이메일: {}]", email);
    }


}