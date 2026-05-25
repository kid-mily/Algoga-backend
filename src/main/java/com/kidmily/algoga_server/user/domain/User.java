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