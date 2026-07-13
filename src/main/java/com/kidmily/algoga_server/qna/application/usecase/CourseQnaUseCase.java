package com.kidmily.algoga_server.qna.application.usecase;

import com.kidmily.algoga_server.qna.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.qna.application.result.CourseQnaCommentResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaResult;

import java.util.List;

/**
 * 강의 Q&A 유스케이스. CourseService에 섞여 있던 Q&A 책임을 qna 도메인으로 분리하기 위한 진입 인터페이스.
 *
 * <p>메서드 시그니처와 반환 타입은 기존 CourseUseCase의 Q&A 메서드와 동일하여 API 계약 변화가 없다.
 */
public interface CourseQnaUseCase {

    CourseQnaResult createQna(CreateCourseQnaCommand command);

    List<CourseQnaResult> getQnas(Long courseId);

    CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId);

    CourseQnaResult answerQna(AnswerCourseQnaCommand command);

    CourseQnaCommentResult createComment(CreateCourseQnaCommentCommand command);
}
