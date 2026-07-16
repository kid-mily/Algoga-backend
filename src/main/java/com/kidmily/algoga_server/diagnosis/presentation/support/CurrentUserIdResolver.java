package com.kidmily.algoga_server.diagnosis.presentation.support;

import com.kidmily.algoga_server.diagnosis.exception.DiagnosisErrorCode;
import com.kidmily.algoga_server.diagnosis.exception.DiagnosisException;

import java.lang.reflect.Method;

public final class CurrentUserIdResolver {

    private CurrentUserIdResolver() {
    }

    public static Long resolveRequired(Object principal) {
        Long userId = resolveNullable(principal);
        if (userId == null) {
            throw new DiagnosisException(DiagnosisErrorCode.DIAGNOSIS_LOGIN_REQUIRED);
        }
        return userId;
    }

    public static Long resolveNullable(Object principal) {
        if (principal == null) {
            return null;
        }

        try {
            Object user = invoke(principal, "getUser");
            if (user == null) {
                return null;
            }

            Object id = invoke(user, "getId");
            return id instanceof Long userId ? userId : null;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static Object invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to resolve authenticated user.", exception);
        }
    }
}
