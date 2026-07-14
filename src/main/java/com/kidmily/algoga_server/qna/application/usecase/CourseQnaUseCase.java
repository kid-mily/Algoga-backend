package com.kidmily.algoga_server.qna.application.usecase;

import com.kidmily.algoga_server.qna.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.qna.application.result.CourseQnaCommentResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaResult;

import java.util.List;

public interface CourseQnaUseCase {

    CourseQnaResult createQna(CreateCourseQnaCommand command);

    List<CourseQnaResult> getQnas(Long courseId);

    CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId);

    CourseQnaResult answerQna(AnswerCourseQnaCommand command);

    CourseQnaCommentResult createComment(CreateCourseQnaCommentCommand command);
}
