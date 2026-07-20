package com.kidmily.algoga_server.chatbot.infrastructure.user;

import com.kidmily.algoga_server.chatbot.application.port.UserProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * UserProfilePort 구현체. user 도메인의 UserRepository 를 리플렉션으로 호출해
 * 유저의 이름/닉네임만 얇게 가져온다. (user 도메인에 대한 직접 import 의존을 두지 않기 위함)
 * course/benefit/inquiry 도메인의 UserProfileAdapter 와 동일한 방식.
 */
@Component("chatbotUserProfileAdapter")
@RequiredArgsConstructor
public class UserProfileAdapter implements UserProfilePort {

    private static final String USER_REPOSITORY = "com.kidmily.algoga_server.user.domain.UserRepository";

    private final ApplicationContext applicationContext;

    @Override
    public Optional<UserProfile> findProfile(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        try {
            Object repository = applicationContext.getBean(Class.forName(USER_REPOSITORY));
            Object result = invoke(repository, "findById", userId);
            if (result instanceof Optional<?> optional) {
                return optional.map(user -> new UserProfile(
                        (Long) invoke(user, "getId"),
                        (String) invoke(user, "getName"),
                        (String) invoke(user, "getNickname")
                ));
            }
            return Optional.empty();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return Optional.empty();
        }
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
