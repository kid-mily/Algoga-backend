package com.kidmily.algoga_server.notification.infrastructure.adapter;

import com.kidmily.algoga_server.enrollment.domain.model.EnrollmentStatus;
import com.kidmily.algoga_server.enrollment.infrastructure.persistence.repository.SpringDataEnrollmentRepository;
import com.kidmily.algoga_server.notification.application.port.EnrollmentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component("notificationEnrollmentPortAdapter")
@RequiredArgsConstructor
public class EnrollmentPortAdapter implements EnrollmentPort {

    private final SpringDataEnrollmentRepository enrollmentRepository;

    @Override
    public List<ExpiringEnrollment> findExpiringBetween(LocalDateTime start, LocalDateTime end) {
        return enrollmentRepository
                .findExpiringBetween(EnrollmentStatus.ENROLLED, start, end)
                .stream()
                .map(e -> new ExpiringEnrollment(e.getUserId(), e.getCourseId()))
                .toList();
    }
}