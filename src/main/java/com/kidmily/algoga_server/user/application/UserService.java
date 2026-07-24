package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.global.util.RedisKeys;
import com.kidmily.algoga_server.refund.application.usecase.RefundQueryUseCase;
import com.kidmily.algoga_server.user.domain.SocialType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import com.kidmily.algoga_server.user.presentation.request.UpdatePasswordRequest;
import com.kidmily.algoga_server.user.presentation.request.UpdateProfileRequest;
import com.kidmily.algoga_server.user.presentation.response.AdminUserListResponse;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.UserProfileResponse;
import com.kidmily.algoga_server.user.settings.UserStorageSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
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

    // 소프트딜리트
    private final BookingQueryUseCase bookingQueryUseCase;
    private final RefundQueryUseCase refundQueryUseCase;

    // 마이페이지 이메일 인증
    private final EmailVerificationHelper emailVerificationHelper;

    // 통계 대시보드용 커스텀 지표
    private final Counter userWithdrawTotal;

    // 내 프로필 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
        return UserProfileResponse.from(user);
    }

    // 1. 마이페이지 본인확인용 이메일 발송
    public void sendMyPageVerificationCode(String email) {
        // 이미 로그인된 유저의 이메일이므로 유저 존재 여부만 확인
        userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        emailVerificationHelper.sendCode(
                email,
                RedisKeys.MYPAGE_AUTH_CODE_PREFIX,
                "[ALGOGA] 마이페이지 본인확인 인증번호",
                "안녕하세요, ALGOGA입니다.\n\n정보 수정을 위한 본인확인 인증번호는 다음과 같습니다."
        );
        log.info("마이페이지 본인확인 인증번호 발송 완료 [요청 이메일: {}]", email);
    }

    // 2. 마이페이지 본인확인 인증번호 검증
    public void verifyMyPageEmailCode(String email, String code) {
        // 인증 1번으로 프로필 수정/비밀번호 변경/회원탈퇴 마커를 동시에 발급 -> 각 API가 독립적으로 소비 가능
        emailVerificationHelper.verifyCode(
                email, code, RedisKeys.MYPAGE_AUTH_CODE_PREFIX,
                RedisKeys.MYPAGE_AUTH_SUCCESS_PROFILE_PREFIX,
                RedisKeys.MYPAGE_AUTH_SUCCESS_PASSWORD_PREFIX,
                RedisKeys.MYPAGE_AUTH_SUCCESS_WITHDRAW_PREFIX
        );

        log.info("마이페이지 이메일 인증 성공 [이메일: {}]", email);
    }

    // 내 프로필 수정
    @Transactional
    public AuthTokenResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        // 1. 이메일 인증 완료 여부 확인 (필수, 프로필 수정 전용 마커)
        emailVerificationHelper.assertVerified(email, RedisKeys.MYPAGE_AUTH_SUCCESS_PROFILE_PREFIX);

        // 2. 전화번호 중복 체크 (기존 번호와 다를 때만 체크)
        if (request.phone() != null && !request.phone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.phone())) {
                throw new UserException(UserErrorCode.ALREADY_EXISTS_PHONE);
            }
        }

        // S3 이미지 스토리지 업로드 분기 처리 로직
        String targetImageUrl = user.getProfileImageUrl();
        MultipartFile imageFile = request.profileImage();

        if (imageFile != null && !imageFile.isEmpty()) {
            if (targetImageUrl != null && !targetImageUrl.isBlank()) {
                fileStoragePort.deleteFile(targetImageUrl);
            }
            targetImageUrl = fileStoragePort.uploadFile(
                    imageFile,
                    storageSettings.getDirectory()
            );
            log.info("회원 프로필 이미지 S3 업로드 완료 [이메일: {}]", email);
        }

        // Entity 내부 값 업데이트 (Dirty Checking 유도)
        user.updateProfile(request.nickname(), request.phone(), targetImageUrl, email);

        String newToken = globalJwtProvider.createUserAccessToken(email);

        log.info("프로필 정보 수정 완료 [이메일: {}]", email);

        // 4. 정보 수정이 끝났으니 인증 성공 마커(Redis) 삭제
        redisTemplate.delete(RedisKeys.MYPAGE_AUTH_SUCCESS_PROFILE_PREFIX + email);

        return new AuthTokenResponse(
                newToken,
                null,
                user.getRequiresPasswordChange(),
                user.getNickname(),
                targetImageUrl
        );
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        // 1. 이메일 인증 완료 여부 확인 (비밀번호 변경 전용 마커)
        emailVerificationHelper.assertVerified(email, RedisKeys.MYPAGE_AUTH_SUCCESS_PASSWORD_PREFIX);

        // 2. 소셜 로그인 유저 방어
        if (user.getSocialType() != SocialType.LOCAL) { // LOCAL이 아닌 유저는 변경 불가
            throw new UserException(UserErrorCode.SOCIAL_USER_PASSWORD_CHANGE_NOT_ALLOWED);
        }

        // 3. 기존 비밀번호가 맞는지 확인 (기존 로직 유지)
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("비밀번호 변경 실패: 현재 비밀번호 불일치 [이메일: {}]", email);
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        // 4. 기존 비밀번호와 새 비밀번호가 같은지 확인
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new UserException(UserErrorCode.PASSWORD_SAME_AS_OLD);
        }

        // 5. 새 비밀번호 암호화 후 변경 (기존 로직 유지)
        user.updatePassword(passwordEncoder.encode(request.newPassword()));

        log.info("회원 비밀번호 변경 완료 [이메일: {}]", email);

        // 6. 보안을 위해 비밀번호 변경 후 인증 상태 초기화
        redisTemplate.delete(RedisKeys.MYPAGE_AUTH_SUCCESS_PASSWORD_PREFIX + email);
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

        // 진행 중인 예약/환불 여부를 이메일 인증보다 먼저 확인한다.
        // (순서가 반대였을 때는 탈퇴가 애초에 불가능한 사용자도 인증 절차부터 다 거친 뒤에야
        //  예약/환불 때문에 막힌다는 걸 알게 되는 불필요한 흐름이었음)
        if (bookingQueryUseCase.hasActiveBooking(user.getId())) {
            throw new UserException(UserErrorCode.ACTIVE_BOOKING_EXISTS);
        }
        if (refundQueryUseCase.hasActiveRefund(user.getId())) {
            throw new UserException(UserErrorCode.ACTIVE_REFUND_EXISTS);
        }

        // 이메일 인증 완료 여부 확인 (회원탈퇴 전용 마커, 프로필/비밀번호 변경과 독립적으로 소비)
        emailVerificationHelper.assertVerified(email, RedisKeys.MYPAGE_AUTH_SUCCESS_WITHDRAW_PREFIX);

        // 유저 엔티티 Soft Delete 처리 (탈퇴 상태, 날짜 기록, 이메일 랜덤 변경)
        user.withdraw();

        // 로그아웃 처리 (Redis 토큰 삭제 및 블랙리스트 등록으로 즉시 쫓아냄)
        // 이메일이 이미 _deleted_ 로 바뀌었으므로, 원래 email 변수를 사용해 지워줍니다.
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + email);

        // 탈퇴가 끝났으니 인증 성공 마커(Redis) 삭제
        redisTemplate.delete(RedisKeys.MYPAGE_AUTH_SUCCESS_WITHDRAW_PREFIX + email);

        // 탈퇴 후 30일간 같은 이메일로 재가입 못 하도록 쿨다운 마커 설정 (TTL 지나면 자동 해제)
        redisTemplate.opsForValue().set(RedisKeys.WITHDRAWN_EMAIL_PREFIX + email, "true", 30, TimeUnit.DAYS);

        // 탈퇴 이벤트 퍼블리싱
        // 이제 다른 도메인(쿠폰, 예약 등) 담당자들이 이 이벤트를 듣고 각자 데이터를 지웁니다.
        eventPublisher.publishEvent(new UserWithdrawnEvent(user.getId(), email));

        userWithdrawTotal.increment();
        log.info("회원 탈퇴 처리 완료 및 이벤트 발행 [기존 이메일: {}, 식별자: {}]", email, user.getId());
    }

    // 관리자용: 전체 유저 리스트 조회 로직
    @Transactional(readOnly = true)
    public Page<User> getAdminUserListRaw(Pageable pageable) {
        return userRepository.findByIsDeletedFalse(pageable);
    }

    // 관리자용: 특정 유저 상세 조회 (기본 정보 + 로그인상태 + 친구 목록)
    @Transactional(readOnly = true)
    public User getAdminUserDetailRaw(Long targetUserId) {
        return userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
    }

    public boolean isUserOnline(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.REFRESH_TOKEN_PREFIX + email));
    }

}