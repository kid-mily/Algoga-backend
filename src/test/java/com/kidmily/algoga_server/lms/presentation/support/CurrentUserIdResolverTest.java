package com.kidmily.algoga_server.lms.presentation.support;

import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserIdResolverTest {

    @Test
    void requiresLoginWhenPrincipalIsMissing() {
        LmsException exception = assertThrows(
                LmsException.class,
                () -> CurrentUserIdResolver.resolveRequired(null)
        );

        assertSame(LmsErrorCode.DIAGNOSIS_LOGIN_REQUIRED, exception.getErrorCode());
    }

    @Test
    void resolvesUserIdFromAuthenticatedPrincipal() {
        Long userId = CurrentUserIdResolver.resolveRequired(new TestPrincipal(new TestUser(2L)));

        assertEquals(2L, userId);
    }

    public record TestPrincipal(TestUser user) {
        public TestUser getUser() {
            return user;
        }
    }

    public record TestUser(Long id) {
        public Long getId() {
            return id;
        }

        public String getName() {
            return "test user";
        }
    }
}
