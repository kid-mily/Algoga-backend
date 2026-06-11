package com.kidmily.algoga_server.lms.infrastructure.payment;

import com.kidmily.algoga_server.lms.application.port.PaymentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentAdapter implements PaymentPort {

    private static final String PAYMENT_REPOSITORY = "com.kidmily.algoga_server.payment.domain.repository.PaymentRepository";
    private static final String PAYMENT_TYPE = "com.kidmily.algoga_server.payment.domain.model.PaymentType";
    private static final String PAYMENT_STATUS = "com.kidmily.algoga_server.payment.domain.model.PaymentStatus";

    private final ApplicationContext applicationContext;

    @Override
    public List<Long> findPaidCourseIds(Long userId) {
        if (userId == null) {
            return List.of();
        }

        return findLecturePayments(userId)
                .stream()
                .map(payment -> (Long) invoke(payment, "getCourseId"))
                .filter(courseId -> courseId != null)
                .toList();
    }

    @Override
    public long countPaidUsersByCourse(Long courseId) {
        if (courseId == null) {
            return 0;
        }

        try {
            Object repository = paymentRepository();
            Method method = findMethod(repository.getClass(), "countByCourseIdAndPaymentTypeAndStatus", 3);
            Object result = method.invoke(repository, courseId, enumValue(PAYMENT_TYPE, "LECTURE_ONLY"), enumValue(PAYMENT_STATUS, "SUCCESS"));
            return result instanceof Number number ? number.longValue() : 0;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return 0;
        }
    }

    private List<Object> findLecturePayments(Long userId) {
        try {
            Object repository = paymentRepository();
            Method method = findMethod(repository.getClass(), "findByUserIdAndPaymentTypeAndStatusAndCourseIdIsNotNull", 3);
            Object result = method.invoke(repository, userId, enumValue(PAYMENT_TYPE, "LECTURE_ONLY"), enumValue(PAYMENT_STATUS, "SUCCESS"));
            if (result instanceof List<?> list) {
                return list.stream().map(Object.class::cast).toList();
            }
            return List.of();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return List.of();
        }
    }

    private Object paymentRepository() throws ClassNotFoundException {
        return applicationContext.getBean(Class.forName(PAYMENT_REPOSITORY));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object enumValue(String className, String value) throws ClassNotFoundException {
        Class<?> enumType = Class.forName(className);
        return Enum.valueOf((Class<Enum>) enumType.asSubclass(Enum.class), value);
    }

    private Object invoke(Object target, String methodName, Object... args) {
        try {
            Method method = findMethod(target.getClass(), methodName, args.length);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            return null;
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
