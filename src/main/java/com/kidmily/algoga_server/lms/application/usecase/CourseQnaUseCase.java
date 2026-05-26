package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;

import java.util.List;

public interface CourseQnaUseCase {

    CourseQna createQna(CreateCourseQnaCommand command);

    List<CourseQna> getQnas(Long courseId);

    CourseQna answerQna(AnswerCourseQnaCommand command);
}