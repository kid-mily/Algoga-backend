package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import com.kidmily.algoga_server.user.presentation.request.UpdatePasswordRequest;
import com.kidmily.algoga_server.user.presentation.request.UpdateProfileRequest;
import com.kidmily.algoga_server.user.presentation.request.VerifyPasswordRequest;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.UserProfileResponse;
import com.kidmily.algoga_server.user.settings.UserStorageSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalJwtProvider globalJwtProvider;

    // S3 스토리지 공통 포트 및 유저 세팅 인프라 빈 주입
    private final FileStoragePort fileStoragePort;
    private final UserStorageSettings storageSettings;

    // 내 프로필 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
        return UserProfileResponse.from(user);
    }

    // 정보 수정 진입 전 비밀번호 검증
    @Transactional(readOnly = true)
    public void verifyPassword(String email, VerifyPasswordRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("정보 수정 진입 전 비밀번호 검증 실패 [이메일: {}]", email);
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        log.info("정보 수정 진입 전 비밀번호 검증 성공 [이메일: {}]", email);
    }

    // 내 프로필 수정
    @Transactional
    public AuthTokenResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        String newEmail = (request.email() != null) ? request.email() : user.getEmail();

        // 이메일 변경 시 중복 체크
        if (!newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {

                log.warn("프로필 수정 중 이메일 중복 발생 [기존: {}, 변경 시도: {}]", user.getEmail(), newEmail);
                throw new UserException(UserErrorCode.ALREADY_EXISTS_EMAIL);
            }

            log.info("회원 이메일 변경 진행 [기존: {} -> 새 이메일: {}]", user.getEmail(), newEmail);
        }

        // S3 이미지 스토리지 업로드 분기 처리 로직
        String targetImageUrl = user.getProfileImageUrl();
        MultipartFile imageFile = request.profileImage();

        if (imageFile != null && !imageFile.isEmpty()) {
            if (targetImageUrl != null && !targetImageUrl.isBlank()) {
                fileStoragePort.deleteFile(storageSettings.getBucketName(), targetImageUrl);
            }
            targetImageUrl = fileStoragePort.uploadFile(
                    imageFile,
                    storageSettings.getBucketName(),
                    storageSettings.getDirectory()
            );
            log.info("회원 프로필 이미지 S3 업로드 완료 [이메일: {}]", newEmail);
        }

        // Entity 내부 값 업데이트 (Dirty Checking 유도)
        user.updateProfile(request.nickname(), request.phone(), targetImageUrl, newEmail);

        String newToken = globalJwtProvider.createUserAccessToken(newEmail);

        log.info("프로필 정보 수정 완료 [이메일: {}]", newEmail);

        return new AuthTokenResponse(
                newToken,
                null,
                user.getRequiresPasswordChange()
        );
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        // 기존 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("비밀번호 변경 실패: 현재 비밀번호 불일치 [이메일: {}]", email);
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 암호화 후 변경
        user.updatePassword(passwordEncoder.encode(request.newPassword()));

        log.info("회원 비밀번호 변경 완료 [이메일: {}]", email);
    }

    // 회원 탈퇴 (Soft Delete)
    public void withdraw(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (user.isDeleted()) {
            log.warn("비정상적 접근: 이미 탈퇴한 계정에 탈퇴 요청 [이메일: {}]", email);
            throw new UserException(UserErrorCode.DELETED_USER);
        }

        user.withdraw(); // User 엔티티의 isDeleted 상태를 true로 변경

        // @Transactional 안에서 엔티티 값이 변경되면 JPA가 알아서 DB에 UPDATE 쿼리를 날립니다! (더티 체킹)
        log.info("회원 탈퇴 처리 완료 [이메일: {}]", email);
    }
}