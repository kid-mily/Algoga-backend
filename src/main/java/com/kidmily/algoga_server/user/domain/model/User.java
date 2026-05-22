package com.kidmily.algoga_server.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 리컨스티튜트를 위한 기본 생성자
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;
    private String nickname;
    private SocialType socialType;
    private String personalCode;
    private int loginFailCount;
    private LocalDateTime lockedUntil;
    private boolean isDeleted;

    // 신규 생성 (회원가입 시)
    private User(String email, String password, String name, String phone, LocalDate birthDate, Gender gender, String nickname) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.nickname = nickname;
        this.socialType = SocialType.LOCAL; // 기본은 로컬 가입
        this.personalCode = UUID.randomUUID().toString();
        this.loginFailCount = 0;
        this.isDeleted = false;
    }

    public static User create(String email, String password, String name, String phone, LocalDate birthDate, Gender gender, String nickname) {
        return new User(email, password, name, phone, birthDate, gender, nickname);
    }

    // DB에서 불러올 때 (MapStruct 사용 시)
    public static User reconstitute(Long id, String email, String password, String name, String phone, LocalDate birthDate,
                                    Gender gender, String nickname, SocialType socialType, String personalCode,
                                    int loginFailCount, LocalDateTime lockedUntil, boolean isDeleted) {
        User user = new User();
        user.id = id;
        user.email = email;
        user.password = password;
        user.name = name;
        user.phone = phone;
        user.birthDate = birthDate;
        user.gender = gender;
        user.nickname = nickname;
        user.socialType = socialType;
        user.personalCode = personalCode;
        user.loginFailCount = loginFailCount;
        user.lockedUntil = lockedUntil;
        user.isDeleted = isDeleted;
        return user;
    }

    public void increaseLoginFailCount() {
        this.loginFailCount++;
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
    }
}