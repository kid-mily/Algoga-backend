package com.kidmily.algoga_server.notification.application.port;

import java.time.LocalDateTime;
import java.util.List;

public interface EnrollmentPort {

    List<ExpiringEnrollment> findExpiringBetween(LocalDateTime start, LocalDateTime end);

    record ExpiringEnrollment(Long userId, Long courseId) {}
}