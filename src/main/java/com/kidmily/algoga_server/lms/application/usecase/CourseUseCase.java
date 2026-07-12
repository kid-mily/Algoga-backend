package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;

import com.kidmily.algoga_server.qna.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CompleteCourseCommand;
import com.kidmily.algoga_server.course.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.qna.application.result.CourseQnaCommentResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaResult;
import com.kidmily.algoga_server.course.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.course.application.result.CourseResult;
import com.kidmily.algoga_server.course.application.result.CourseClassroomResult;
import com.kidmily.algoga_server.course.application.result.CourseStudentResult;
import com.kidmily.algoga_server.course.application.result.MyCourseResult;
import com.kidmily.algoga_server.lms.application.result.*;
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

    CourseQnaResult createQna(CreateCourseQnaCommand command);

    List<CourseQnaResult> getQnas(Long courseId);

    CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId);

    CourseQnaResult answerQna(AnswerCourseQnaCommand command);

    CourseQnaCommentResult createComment(CreateCourseQnaCommentCommand command);

    List<CourseResult> getPublishedCoursesByCountry(Long countryId);

    CourseResult getPublishedCourse(Long courseId);

    boolean isEnrolled(Long userId, Long courseId);

    boolean isPaid(Long userId, Long courseId);

    List<CourseResult> getRecommendedCoursesByCountryAndLevel(Long countryId, String level);

    long countPublishedCoursesByCountry(Long countryId);

    Map<Long, Long> countPublishedCoursesByCountryIds(List<Long> countryIds);
}