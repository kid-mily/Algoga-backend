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
import com.kidmily.algoga_server.user.presentation.response.SignupPathStatResponse;
import com.kidmily.algoga_server.user.presentation.response.UserProfileResponse;
import com.kidmily.algoga_server.user.settings.UserStorageSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    // softDelete 정책 관련
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;

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
    @Transactional
    public void withdraw(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (user.isDeleted()) {
            log.warn("비정상적 접근: 이미 탈퇴한 계정에 탈퇴 요청 [이메일: {}]", email);
            throw new UserException(UserErrorCode.DELETED_USER);
        }

        // [TODO] 팀원들이 예약/환불 인터페이스를 만들어주면 이 주석을 풀고 사용하세요!
//         if (bookingQueryUseCase.hasActiveBooking(user.getId())) {
//         throw new UserException(UserErrorCode.ACTIVE_BOOKING_EXISTS);
//         }
//         if (refundQueryUseCase.hasActiveRefund(user.getId())) {
//         throw new UserException(UserErrorCode.ACTIVE_REFUND_EXISTS);
//         }
//
        // 유저 엔티티 Soft Delete 처리 (탈퇴 상태, 날짜 기록, 이메일 랜덤 변경)
        user.withdraw();

        // 로그아웃 처리 (Redis 토큰 삭제 및 블랙리스트 등록으로 즉시 쫓아냄)
        // 이메일이 이미 _deleted_ 로 바뀌었으므로, 원래 email 변수를 사용해 지워줍니다.
        redisTemplate.delete("RT:" + email);

        // 탈퇴 이벤트 퍼블리싱
        // 이제 다른 도메인(쿠폰, 예약 등) 담당자들이 이 이벤트를 듣고 각자 데이터를 지웁니다.
        eventPublisher.publishEvent(new UserWithdrawnEvent(user.getId(), email));

        log.info("회원 탈퇴 처리 완료 및 이벤트 발행 [기존 이메일: {}, 식별자: {}]", email, user.getId());
    }

    // 가입 경로 통계 조회 (관리자 통게용)
    @Transactional(readOnly = true)
    public List<SignupPathStatResponse> getSignupPathStats() {
        return userRepository.countUsersBySignupPath().stream()
                .map(SignupPathStatResponse::from)
                .toList();
    }
}