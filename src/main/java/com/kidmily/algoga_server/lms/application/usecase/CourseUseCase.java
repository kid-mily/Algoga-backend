package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CompleteCourseCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.lms.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.lms.application.result.CourseStudentResult;
import com.kidmily.algoga_server.lms.application.result.MyCourseResult;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;
import com.kidmily.algoga_server.lms.domain.model.CourseQnaComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CourseUseCase {

    Long createCourse(CreateCourseCommand command);

    Page<Course> getCourses(Pageable pageable);

    Course getCourse(Long courseId);

    Course updateCourse(Long courseId, UpdateCourseCommand command);

    void deleteCourse(Long courseId);

    CourseCompletion completeCourse(CompleteCourseCommand command);

    List<CourseStudentResult> getCourseStudents(Long courseId);

    List<MyCourseResult> getMyCourses(Long userId);

    CourseQna createQna(CreateCourseQnaCommand command);

    List<CourseQna> getQnas(Long courseId);

    CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId);

    CourseQna answerQna(AnswerCourseQnaCommand command);

    CourseQnaComment createComment(CreateCourseQnaCommentCommand command);

    List<Course> getPublishedCoursesByCountry(Long countryId);

    Course getPublishedCourse(Long courseId);

    boolean isEnrolled(Long userId, Long courseId);

    boolean isPaid(Long userId, Long courseId);

    List<Course> getRecommendedCoursesByCountryAndLevel(Long countryId, String level);

    long countPublishedCoursesByCountry(Long countryId);

    Map<Long, Long> countPublishedCoursesByCountryIds(List<Long> countryIds);
}
