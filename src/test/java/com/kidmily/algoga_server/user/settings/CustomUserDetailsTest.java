package com.kidmily.algoga_server.user.settings;

import com.kidmily.algoga_server.user.domain.Gender;
import com.kidmily.algoga_server.user.domain.SocialType;
import com.kidmily.algoga_server.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void exposesUserEmailPasswordAndRole() {
        User user = baseUser();
        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getUser()).isSameAs(user);
        assertThat(details.getUsername()).isEqualTo("user@example.com");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void disabledWhenUserIsWithdrawn() {
        User user = baseUser();
        user.withdraw();

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void lockedWhenUserAccountIsLocked() {
        User user = baseUser();
        for (int i = 0; i < 5; i++) {
            user.increaseLoginFailure();
        }

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.isAccountNonLocked()).isFalse();
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
