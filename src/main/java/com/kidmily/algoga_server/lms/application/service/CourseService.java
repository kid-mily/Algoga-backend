package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService implements CourseUseCase {

    private final CourseRepository courseRepository;
    private final MapRepository mapRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public List<Course> getPublishedCoursesByCountry(Long countryId) {
        validateCountry(countryId);
        return courseRepository.findPublishedByCountryId(countryId);
    }

    @Override
    public Course getPublishedCourse(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COURSE_NOT_FOUND));

        if (!"PUBLISHED".equals(course.getStatus())) {
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }

        return course;
    }

    public boolean isEnrolled(Long userId, Long courseId) {
        if (userId == null) {
            return false;
        }

        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    public boolean isPaid(Long userId, Long courseId) {
        if (userId == null) {
            return false;
        }

        return paymentRepository.findByUserIdAndPaymentTypeAndStatusAndCourseIdIsNotNull(
                        userId,
                        PaymentType.LECTURE_ONLY,
                        PaymentStatus.SUCCESS
                )
                .stream()
                .map(Payment::getCourseId)
                .anyMatch(courseId::equals);
    }

    @Override
    public List<Course> getRecommendedCoursesByCountryAndLevel(Long countryId, String level) {
        validateCountry(countryId);
        validateCourseLevel(level);

        return courseRepository.findPublishedByCountryIdAndLevel(countryId, level);
    }

    @Override
    public long countPublishedCoursesByCountry(Long countryId) {
        validateCountry(countryId);
        return courseRepository.countPublishedByCountryId(countryId);
    }

    @Override
    public Map<Long, Long> countPublishedCoursesByCountryIds(List<Long> countryIds) {
        return courseRepository.countPublishedByCountryIds(countryIds);
    }

    private void validateCountry(Long countryId) {
        mapRepository.findActiveCountryById(countryId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.COUNTRY_NOT_FOUND));
    }

    private void validateCourseLevel(String level) {
        if (!"BEGINNER".equals(level)
                && !"INTERMEDIATE".equals(level)
                && !"ADVANCED".equals(level)) {
            throw new LmsException(LmsErrorCode.INVALID_COURSE_LEVEL);
        }
    }
}
