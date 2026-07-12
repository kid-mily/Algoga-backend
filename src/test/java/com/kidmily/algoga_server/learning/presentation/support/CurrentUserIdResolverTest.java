package com.kidmily.algoga_server.learning.presentation.support;

import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserIdResolverTest {

    @Test
    void requiresLoginWhenPrincipalIsMissing() {
        LearningException exception = assertThrows(
                LearningException.class,
                () -> CurrentUserIdResolver.resolveRequired(null)
        );

        assertSame(LearningErrorCode.DIAGNOSIS_LOGIN_REQUIRED, exception.getErrorCode());
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
