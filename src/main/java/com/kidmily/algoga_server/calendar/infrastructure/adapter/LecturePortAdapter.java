package com.kidmily.algoga_server.calendar.infrastructure.adapter;

import com.kidmily.algoga_server.calendar.application.port.LecturePort;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LecturePortAdapter implements LecturePort {

    private final CourseRepository courseRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public String getLectureName(Long lectureId) {
        return courseRepository.findById(lectureId)
                .map(course -> course.getTitle())
                .orElse("알 수 없는 강의");
    }
    @Override
    public LocalDate getLectureStartDate(Long lectureId, Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .filter(p -> p.getCourseId() != null
                        && p.getCourseId().equals(lectureId)
                        && p.getStatus() == PaymentStatus.SUCCESS)
                .findFirst()
                .map(p -> p.getCreatedAt().toLocalDate())
                .orElse(null);
    }

    @Override
    public LocalDate getLectureEndDate(Long lectureId, Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .filter(p -> p.getCourseId() != null
                        && p.getCourseId().equals(lectureId)
                        && p.getStatus() == PaymentStatus.SUCCESS)
                .findFirst()
                .map(p -> p.getCreatedAt().toLocalDate().plusMonths(6))
                .orElse(null);
    }
}