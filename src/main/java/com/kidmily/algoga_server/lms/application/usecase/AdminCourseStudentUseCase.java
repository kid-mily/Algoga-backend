package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.result.CourseStudentResult;

import java.util.List;

public interface AdminCourseStudentUseCase {

    List<CourseStudentResult> getCourseStudents(Long courseId);
}