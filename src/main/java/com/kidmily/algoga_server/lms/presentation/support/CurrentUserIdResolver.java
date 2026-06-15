package com.kidmily.algoga_server.lms.presentation.support;

import java.lang.reflect.Method;

public final class CurrentUserIdResolver {

    private CurrentUserIdResolver() {
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

    public static String resolveNameNullable(Object principal) {
        if (principal == null) {
            return null;
        }

        try {
            Object user = invoke(principal, "getUser");
            if (user == null) {
                return null;
            }

            Object name = invoke(user, "getName");
            return name instanceof String userName ? userName : null;
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
