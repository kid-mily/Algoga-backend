package com.kidmily.algoga_server.benefit.infrastructure.lms;

import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.QuizSubmission;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LmsCourseAdapter implements LmsCoursePort {

    private final CourseRepository courseRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final MapRepository mapRepository;

    @Override
    public void validateCourseExists(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            throw new BenefitException(BenefitErrorCode.COURSE_NOT_FOUND);
        }
    }

    @Override
    public CourseRewardInfo getCourseRewardInfo(Long userId, Long courseId) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.COURSE_NOT_FOUND));

        if (!courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new BenefitException(BenefitErrorCode.COURSE_COMPLETION_NOT_FOUND);
        }

        QuizSubmission quizSubmission = quizSubmissionRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.QUIZ_NOT_SUBMITTED));

        return new CourseRewardInfo(
                course.getId(),
                course.getPrice(),
                quizSubmission.getCorrectCount()
        );
    }

    @Override
    public Optional<CourseSummary> findCourseSummary(Long courseId) {
        return courseRepository.findByIdAndDeletedFalse(courseId)
                .map(course -> {
                    String countryName = mapRepository.findActiveCountryById(course.getCountryId())
                            .map(Country::getName)
                            .orElse(null);

                    return new CourseSummary(
                            course.getId(),
                            course.getTitle(),
                            course.getCountryId(),
                            countryName
                    );
                });
    }

    @Override
    public boolean existsCountry(Long countryId) {
        return mapRepository.findActiveCountryById(countryId).isPresent();
    }
}