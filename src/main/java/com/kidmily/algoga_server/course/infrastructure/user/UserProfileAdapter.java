package com.kidmily.algoga_server.course.infrastructure.user;

import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Component("lmsUserProfileAdapter")
@RequiredArgsConstructor
public class UserProfileAdapter implements UserProfilePort {

    private static final String USER_REPOSITORY = "com.kidmily.algoga_server.user.domain.UserRepository";

    private final ApplicationContext applicationContext;

    @Override
    public Optional<UserProfile> findProfile(Long userId) {
        return findUser(userId)
                .map(this::toProfile);
    }

    @Override
    public void updateDiagnosisResult(Long userId, Long countryId, String level, Integer score) {
        Object user = findUser(userId).orElse(null);
        if (user == null) {
            return;
        }

        invoke(user, "updateDiagnosisResult", countryId, level, score);
    }

    private Optional<Object> findUser(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        try {
            Object repository = applicationContext.getBean(Class.forName(USER_REPOSITORY));
            Object result = invoke(repository, "findById", userId);
            if (result instanceof Optional<?> optional) {
                return optional.map(Object.class::cast);
            }
            return Optional.empty();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return Optional.empty();
        }
    }

    private UserProfile toProfile(Object user) {
        return new UserProfile(
                (Long) invoke(user, "getId"),
                (String) invoke(user, "getUsername"),
                (String) invoke(user, "getName"),
                (String) invoke(user, "getEmail"),
                (String) invoke(user, "getNickname")
        );
    }

    private Object invoke(Object target, String methodName, Object... args) {
        try {
            Method method = findMethod(target.getClass(), methodName, args.length);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke user dependency: " + methodName, exception);
        }
    }

    private Method findMethod(Class<?> type, String methodName, int parameterCount) throws NoSuchMethodException {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                return method;
            }
        }
        throw new NoSuchMethodException(methodName);
    }
}