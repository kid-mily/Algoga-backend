package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.application.port.CoursePort;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoursePortAdapter implements CoursePort {
    private final CourseRepository courseRepository;


    @Override
    public String getCourseName(Long courseId) {
        if (courseId == null) return null;
        return courseRepository.findById(courseId)
                .map(course -> course.getTitle())
                .orElse("알 수 없는 강의");
    }
}