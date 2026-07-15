package com.kidmily.algoga_server.diagnosis.presentation.support;

import com.kidmily.algoga_server.diagnosis.exception.DiagnosisErrorCode;
import com.kidmily.algoga_server.diagnosis.exception.DiagnosisException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserIdResolverTest {

    @Test
    void requiresLoginWhenPrincipalIsMissing() {
        DiagnosisException exception = assertThrows(
                DiagnosisException.class,
                () -> CurrentUserIdResolver.resolveRequired(null)
        );

        assertSame(DiagnosisErrorCode.DIAGNOSIS_LOGIN_REQUIRED, exception.getErrorCode());
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
