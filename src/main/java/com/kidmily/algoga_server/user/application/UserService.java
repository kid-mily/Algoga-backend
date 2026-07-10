package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.global.infrastructure.mail.EmailSender;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.global.security.GlobalJwtProvider;
import com.kidmily.algoga_server.refund.application.usecase.RefundQueryUseCase;
import com.kidmily.algoga_server.user.domain.SocialType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.exception.AuthException;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import com.kidmily.algoga_server.user.presentation.request.UpdatePasswordRequest;
import com.kidmily.algoga_server.user.presentation.request.UpdateProfileRequest;
import com.kidmily.algoga_server.user.presentation.request.VerifyPasswordRequest;
import com.kidmily.algoga_server.user.presentation.response.AdminUserListResponse;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse;
import com.kidmily.algoga_server.user.presentation.response.SignupPathStatResponse;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

    // 소프트딜리트
    private final BookingQueryUseCase bookingQueryUseCase;
    private final RefundQueryUseCase refundQueryUseCase;

    // 마이페이지 이메일 인증
    private final EmailSender emailSender;

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
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        String code = String.valueOf((int) (Math.random() * 899999) + 100000);
        redisTemplate.opsForValue().set("MYPAGE_AUTH_CODE:" + email, code, 3, TimeUnit.MINUTES);

        String subject = "[ALGOGA] 마이페이지 본인확인 인증번호";
        String body = "안녕하세요, ALGOGA입니다.\n\n"
                + "정보 수정을 위한 본인확인 인증번호는 다음과 같습니다.\n"
                + "인증번호 : [" + code + "]\n\n"
                + "3분 이내에 입력해 주세요.";

        emailSender.sendEmail(email, subject, body);
        log.info("마이페이지 본인확인 인증번호 발송 완료 [요청 이메일: {}]", email);
    }

    // 2. 마이페이지 본인확인 인증번호 검증
    public void verifyMyPageEmailCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get("MYPAGE_AUTH_CODE:" + email);

        if (savedCode == null || !savedCode.equals(code)) {
            throw new AuthException(AuthErrorCode.EMAIL_AUTH_CODE_MISMATCH);
        }

        redisTemplate.delete("MYPAGE_AUTH_CODE:" + email);
        // 인증 성공 마커를 30분간 유지 (이 마커가 있어야 정보 수정/비번 변경 가능)
        redisTemplate.opsForValue().set("MYPAGE_AUTH_SUCCESS:" + email, "true", 30, TimeUnit.MINUTES);

        log.info("마이페이지 이메일 인증 성공 [이메일: {}]", email);
    }

    // 내 프로필 수정
    @Transactional
    public AuthTokenResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        // 1. 이메일 인증 완료 여부 확인 (필수)
        String isVerified = redisTemplate.opsForValue().get("MYPAGE_AUTH_SUCCESS:" + email);
        if (!"true".equals(isVerified)) {
            throw new UserException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }

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
                fileStoragePort.deleteFile(storageSettings.getBucketName(), targetImageUrl);
            }
            targetImageUrl = fileStoragePort.uploadFile(
                    imageFile,
                    storageSettings.getBucketName(),
                    storageSettings.getDirectory()
            );
            log.info("회원 프로필 이미지 S3 업로드 완료 [이메일: {}]", email);
        }

        // Entity 내부 값 업데이트 (Dirty Checking 유도)
        user.updateProfile(request.nickname(), request.phone(), targetImageUrl, email);

        String newToken = globalJwtProvider.createUserAccessToken(email);

        log.info("프로필 정보 수정 완료 [이메일: {}]", email);

        // 4. 정보 수정이 끝났으니 인증 성공 마커(Redis) 삭제
        redisTemplate.delete("MYPAGE_AUTH_SUCCESS:" + email);

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

        // 1. 이메일 인증 완료 여부 확인 (비밀번호 변경 시에도 필수)
        String isVerified = redisTemplate.opsForValue().get("MYPAGE_AUTH_SUCCESS:" + email);
        if (!"true".equals(isVerified)) {
            throw new UserException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }

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
        redisTemplate.delete("MYPAGE_AUTH_SUCCESS:" + email);
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

        // 회원 탈퇴 할 때 예약/환불
         if (bookingQueryUseCase.hasActiveBooking(user.getId())) {
         throw new UserException(UserErrorCode.ACTIVE_BOOKING_EXISTS);
         }
         if (refundQueryUseCase.hasActiveRefund(user.getId())) {
         throw new UserException(UserErrorCode.ACTIVE_REFUND_EXISTS);
         }

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

    // 가입 경로 통계 조회 (관리자 통게용, 가입일 기준 기간 필터)
    @Transactional(readOnly = true)
    public List<SignupPathStatResponse> getSignupPathStats(LocalDateTime from, LocalDateTime to) {
        return userRepository.countUsersBySignupPath(from, to).stream()
                .map(SignupPathStatResponse::from)
                .toList();
    }

    // 가입 경로 통계 CSV 내보내기 (엑셀에서 바로 열리도록 UTF-8 BOM 포함)
    @Transactional(readOnly = true)
    public byte[] getSignupPathStatsCsv(LocalDateTime from, LocalDateTime to) {
        List<SignupPathStatResponse> stats = getSignupPathStats(from, to);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // BOM for Excel UTF-8

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            writer.println("유입 경로,가입자 수");
            for (SignupPathStatResponse item : stats) {
                writer.printf("%s,%d%n", item.path(), item.count());
            }
        }

        return baos.toByteArray();
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
        return Boolean.TRUE.equals(redisTemplate.hasKey("RT:" + email));
    }

}