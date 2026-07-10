package com.kidmily.algoga_server.notification.infrastructure.adapter;

import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.notification.application.port.CoursePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("notificationCoursePortAdapter")
@RequiredArgsConstructor
public class CoursePortAdapter implements CoursePort {

    private final CourseRepository courseRepository;

    @Override
    public String getCourseName(Long courseId) {
        if (courseId == null) return "강의";
        return courseRepository.findByIdAndDeletedFalse(courseId)
                .map(course -> course.getTitle())
                .orElse("강의");
    }
}