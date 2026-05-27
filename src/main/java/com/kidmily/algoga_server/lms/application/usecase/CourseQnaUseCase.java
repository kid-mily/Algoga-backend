package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.lms.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;
import com.kidmily.algoga_server.lms.domain.model.CourseQnaComment;

import java.util.List;

public interface CourseQnaUseCase {

    CourseQna createQna(CreateCourseQnaCommand command);

    List<CourseQna> getQnas(Long courseId);

    CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId);

    CourseQna answerQna(AnswerCourseQnaCommand command);

    CourseQnaComment createComment(CreateCourseQnaCommentCommand command);
}