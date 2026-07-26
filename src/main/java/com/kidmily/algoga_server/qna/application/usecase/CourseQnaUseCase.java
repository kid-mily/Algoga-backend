package com.kidmily.algoga_server.qna.application.usecase;

import com.kidmily.algoga_server.qna.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.qna.application.result.AdminCourseQnaListItemResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaCommentResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseQnaUseCase {

    CourseQnaResult createQna(CreateCourseQnaCommand command);

    Page<CourseQnaResult> getQnas(Long courseId, Pageable pageable);

    Page<AdminCourseQnaListItemResult> getAdminQnas(Long courseId, Boolean answered, String keyword, Pageable pageable);

    CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId);

    CourseQnaResult answerQna(AnswerCourseQnaCommand command);

    CourseQnaCommentResult createComment(CreateCourseQnaCommentCommand command);
}
