package com.kidmily.algoga_server.benefit.infrastructure.lms;

import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LmsCourseAdapter implements LmsCoursePort {

    private static final String COURSE_REPOSITORY = "com.kidmily.algoga_server.lms.domain.repository.CourseRepository";
    private static final String COURSE_COMPLETION_REPOSITORY = "com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository";
    private static final String QUIZ_SUBMISSION_REPOSITORY = "com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository";
    private static final String MAP_REPOSITORY = "com.kidmily.algoga_server.lms.domain.repository.MapRepository";

    private final ApplicationContext applicationContext;

    @Override
    public void validateCourseExists(Long courseId) {
        if (findCourse(courseId).isEmpty()) {
            throw new BenefitException(BenefitErrorCode.COURSE_NOT_FOUND);
        }
    }

    @Override
    public CourseRewardInfo getCourseRewardInfo(Long userId, Long courseId) {
        Object course = findCourse(courseId)
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.COURSE_NOT_FOUND));

        boolean completed = invoke(
                bean(COURSE_COMPLETION_REPOSITORY),
                "existsByUserIdAndCourseId",
                new Class<?>[]{Long.class, Long.class},
                userId,
                courseId
        );

        if (!completed) {
            throw new BenefitException(BenefitErrorCode.COURSE_COMPLETION_NOT_FOUND);
        }

        Optional<Object> quizSubmission = invoke(
                bean(QUIZ_SUBMISSION_REPOSITORY),
                "findByUserIdAndCourseId",
                new Class<?>[]{Long.class, Long.class},
                userId,
                courseId
        );

        Object submission = quizSubmission
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.QUIZ_NOT_SUBMITTED));

        return new CourseRewardInfo(
                invokeNoArg(course, "getId"),
                invokeNoArg(course, "getPrice"),
                invokeNoArg(submission, "getCorrectCount")
        );
    }

    @Override
    public Optional<CourseSummary> findCourseSummary(Long courseId) {
        return findCourse(courseId)
                .map(course -> {
                    Long countryId = invokeNoArg(course, "getCountryId");
                    String countryName = findCountryName(countryId).orElse(null);

                    return new CourseSummary(
                            invokeNoArg(course, "getId"),
                            invokeNoArg(course, "getTitle"),
                            countryId,
                            countryName
                    );
                });
    }

    @Override
    public boolean existsCountry(Long countryId) {
        return findCountry(countryId).isPresent();
    }

    private Optional<Object> findCourse(Long courseId) {
        return invoke(
                bean(COURSE_REPOSITORY),
                "findByIdAndDeletedFalse",
                new Class<?>[]{Long.class},
                courseId
        );
    }

    private Optional<String> findCountryName(Long countryId) {
        return findCountry(countryId)
                .map(country -> invokeNoArg(country, "getName"));
    }

    private Optional<Object> findCountry(Long countryId) {
        return invoke(
                bean(MAP_REPOSITORY),
                "findActiveCountryById",
                new Class<?>[]{Long.class},
                countryId
        );
    }

    private Object bean(String className) {
        try {
            return applicationContext.getBean(Class.forName(className));
        } catch (ClassNotFoundException exception) {
            throw new BenefitException(BenefitErrorCode.COURSE_NOT_FOUND);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            return (T) method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new BenefitException(BenefitErrorCode.COURSE_NOT_FOUND);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return (T) method.invoke(target);
        } catch (ReflectiveOperationException exception) {
            throw new BenefitException(BenefitErrorCode.COURSE_NOT_FOUND);
        }
    }
}
