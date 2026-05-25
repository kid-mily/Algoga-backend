package com.kidmily.algoga_server.user.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 아이디

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private SocialType socialType;

    @Column(name = "personal_code")
    private String personalCode;

    @Column(name = "login_fail_count")
    private int loginFailCount;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    private String referralCode;
    private String signupPath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    // 비밀번호 찾기해서 임시비번으로 로그인 하면 강제 비번 변경
    @Column(name = "requires_password_change")
    private Boolean requiresPasswordChange = false;

    // 기존 코드 아래에 이 메서드들을 추가하세요.
    public void setTemporaryPassword(String encodedPassword) {
        this.password = encodedPassword;
        this.requiresPasswordChange = true;
        this.loginFailCount = 0; // 비밀번호가 초기화되었으니 실패 카운트도 리셋
        this.lockedUntil = null;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.requiresPasswordChange = false; // 비밀번호를 정상 변경했으므로 플래그 해제
    }

    // 회원 탈퇴 (Soft Delete + 데이터 충돌 방지)
    public void withdraw() {
        this.isDeleted = true;

        // 고유 식별자를 생성하여 기존 데이터 뒤에 붙여줍니다. (이메일/아이디 재가입 허용을 위함)
        String deleteStr = "_deleted_" + java.util.UUID.randomUUID().toString().substring(0, 8);

        this.email = this.email + deleteStr;
        this.username = this.username + deleteStr;
        // 전화번호도 중복 검사
        this.phone = this.phone + deleteStr;
    }


    // 실패 횟수 증가 및 잠금 처리
    public void increaseLoginFailure() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(5); // 5분 잠금
        }
    }

    public void resetLoginFailure() {
        this.loginFailCount = 0;
        this.lockedUntil = null;
    }

    public boolean isAccountLocked() {
        if (this.lockedUntil == null) return false;
        return this.lockedUntil.isAfter(LocalDateTime.now());
    }
}