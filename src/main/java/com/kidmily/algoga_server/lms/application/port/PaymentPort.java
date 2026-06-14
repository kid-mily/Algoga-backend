package com.kidmily.algoga_server.lms.application.port;

import java.util.List;

public interface PaymentPort {

    List<Long> findPaidCourseIds(Long userId);

    long countPaidUsersByCourse(Long courseId);
}
