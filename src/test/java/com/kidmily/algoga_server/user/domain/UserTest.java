package com.kidmily.algoga_server.user.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void increaseLoginFailureLocksAccountAfterFiveFailures() {
        User user = baseUser();

        for (int i = 0; i < 5; i++) {
            user.increaseLoginFailure();
        }

        assertThat(user.getLoginFailCount()).isEqualTo(5);
        assertThat(user.isAccountLocked()).isTrue();
    }

    @Test
    void resetLoginFailureUnlocksAccount() {
        User user = baseUser();
        for (int i = 0; i < 5; i++) {
            user.increaseLoginFailure();
        }

        user.resetLoginFailure();

        assertThat(user.getLoginFailCount()).isZero();
        assertThat(user.isAccountLocked()).isFalse();
    }

    @Test
    void setTemporaryPasswordRequiresPasswordChangeAndClearsLockState() {
        User user = baseUser();
        for (int i = 0; i < 5; i++) {
            user.increaseLoginFailure();
        }

        user.setTemporaryPassword("encoded-temp-password");

        assertThat(user.getPassword()).isEqualTo("encoded-temp-password");
        assertThat(user.getRequiresPasswordChange()).isTrue();
        assertThat(user.getLoginFailCount()).isZero();
        assertThat(user.isAccountLocked()).isFalse();
    }

    @Test
    void updateProfileOnlyChangesNonNullFields() {
        User user = baseUser();

        user.updateProfile("newNick", null, "profile.png", "new@example.com");

        assertThat(user.getNickname()).isEqualTo("newNick");
        assertThat(user.getPhone()).isEqualTo("01012345678");
        assertThat(user.getProfileImageUrl()).isEqualTo("profile.png");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void withdrawSoftDeletesAndChangesUniqueFields() {
        User user = baseUser();
        String originalEmail = user.getEmail();
        String originalUsername = user.getUsername();
        String originalPhone = user.getPhone();

        user.withdraw();

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getEmail()).startsWith(originalEmail + "_deleted_");
        assertThat(user.getUsername()).startsWith(originalUsername + "_deleted_");
        assertThat(user.getPhone()).startsWith(originalPhone + "_deleted_");
    }

    private User baseUser() {
        return User.builder()
                .id(1L)
                .username("user01")
                .email("user@example.com")
                .password("encoded-password")
                .name("Test User")
                .phone("01012345678")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .nickname("nick")
                .socialType(SocialType.LOCAL)
                .personalCode("ABC123")
                .termsServiceAgreed(true)
                .termsPrivacyAgreed(true)
                .termsMarketingAgreed(false)
                .build();
    }
}
