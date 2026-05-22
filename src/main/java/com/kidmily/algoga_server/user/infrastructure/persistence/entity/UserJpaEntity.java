package com.kidmily.algoga_server.user.infrastructure.persistence.entity;

import com.kidmily.algoga_server.user.domain.model.Gender;
import com.kidmily.algoga_server.user.domain.model.SocialType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;
    private String name;
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
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ⭐️ MapStruct가 사용할 Builder (id, createdAt, updatedAt 제외)
    @Builder
    public UserJpaEntity(String email, String password, String name, String phone, LocalDate birthDate,
                         Gender gender, String nickname, SocialType socialType, String personalCode,
                         int loginFailCount, LocalDateTime lockedUntil, boolean isDeleted) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.nickname = nickname;
        this.socialType = socialType;
        this.personalCode = personalCode;
        this.loginFailCount = loginFailCount;
        this.lockedUntil = lockedUntil;
        this.isDeleted = isDeleted;
    }
}