package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.lms.domain.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminContentUseCase {

    Long createCourse(CreateCourseCommand command);

    Page<Course> getCourses(Pageable pageable);

    Course getCourse(Long courseId);

    Course updateCourse(Long courseId, UpdateCourseCommand command);

    void deleteCourse(Long courseId);
}