package com.kidmily.algoga_server.course.application.usecase;

import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;

import com.kidmily.algoga_server.course.application.command.CompleteCourseCommand;
import com.kidmily.algoga_server.course.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.course.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.course.application.result.CourseResult;
import com.kidmily.algoga_server.course.application.result.CourseClassroomResult;
import com.kidmily.algoga_server.course.application.result.CourseStudentResult;
import com.kidmily.algoga_server.course.application.result.MyCourseResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CourseUseCase {

    Long createCourse(CreateCourseCommand command);

    Page<CourseResult> getCourses(Long countryId, String countryName, Pageable pageable);

    Page<CourseResult> getDeletedCourses(Long countryId, String countryName, Pageable pageable);

    CourseResult getCourse(Long courseId);

    CourseResult getDeletedCourse(Long courseId);

    CourseResult updateCourse(Long courseId, UpdateCourseCommand command);

    void deleteCourse(Long courseId);

    CourseCompletionResult completeCourse(CompleteCourseCommand command);

    List<CourseStudentResult> getCourseStudents(Long courseId);

    Page<MyCourseResult> getMyCourses(Long userId, Pageable pageable);

    CourseClassroomResult getCourseClassroom(Long userId, Long courseId);

    List<CourseResult> getPublishedCoursesByCountry(Long countryId);

    CourseResult getPublishedCourse(Long courseId);

    boolean isEnrolled(Long userId, Long courseId);

    boolean isPaid(Long userId, Long courseId);

    List<CourseResult> getRecommendedCoursesByCountryAndLevel(Long countryId, String level);

    List<CourseResult> getOtherLevelCoursesByCountryAndLevel(Long countryId, String level);

    long countPublishedCoursesByCountry(Long countryId);

    Map<Long, Long> countPublishedCoursesByCountryIds(List<Long> countryIds);
}