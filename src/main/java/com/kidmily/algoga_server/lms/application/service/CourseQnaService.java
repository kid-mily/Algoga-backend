package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.lms.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.lms.application.usecase.CourseQnaUseCase;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;
import com.kidmily.algoga_server.lms.domain.model.CourseQnaComment;
import com.kidmily.algoga_server.lms.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseQnaRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseQnaService implements CourseQnaUseCase {

    private final CourseRepository courseRepository;
    private final CourseQnaRepository courseQnaRepository;
    private final CourseQnaCommentRepository courseQnaCommentRepository;

    @Override
    @Transactional
    public CourseQna createQna(CreateCourseQnaCommand command) {
        log.info("[Course Q&A Command] Q&A 등록 요청. courseId={}, userId={}, title={}",
                command.courseId(), command.userId(), command.title());

        validateCourse(command.courseId());

        CourseQna courseQna = CourseQna.create(
                command.courseId(),
                command.userId(),
                command.title(),
                command.question()
        );

        CourseQna savedQna = courseQnaRepository.save(courseQna);

        log.info("[Course Q&A Command] Q&A 등록 완료. qnaId={}, courseId={}, userId={}",
                savedQna.getId(), savedQna.getCourseId(), savedQna.getUserId());

        return savedQna;
    }

    @Override
    public List<CourseQna> getQnas(Long courseId) {
        log.info("[Course Q&A Query] Q&A 목록 조회 요청. courseId={}", courseId);

        validateCourse(courseId);

        List<CourseQna> qnas = courseQnaRepository.findByCourseId(courseId);

        log.info("[Course Q&A Query] Q&A 목록 조회 완료. courseId={}, count={}",
                courseId, qnas.size());

        return qnas;
    }

    @Override
    public CourseQnaDetailResult getQnaDetail(
            Long courseId,
            Long qnaId
    ) {
        log.info("[Course Q&A Query] Q&A 상세 조회 요청. courseId={}, qnaId={}", courseId, qnaId);

        validateCourse(courseId);

        CourseQna qna = findQna(courseId, qnaId);
        List<CourseQnaComment> comments = courseQnaCommentRepository.findByQnaId(qnaId);

        log.info("[Course Q&A Query] Q&A 상세 조회 완료. courseId={}, qnaId={}, commentCount={}",
                courseId, qnaId, comments.size());

        return new CourseQnaDetailResult(
                qna.getId(),
                qna.getCourseId(),
                qna.getUserId(),
                qna.getManagerId(),
                qna.getTitle(),
                qna.getQuestion(),
                qna.getAnswer(),
                qna.getStatus(),
                qna.getCreatedAt(),
                qna.getAnsweredAt(),
                comments
        );
    }

    @Override
    @Transactional
    public CourseQna answerQna(AnswerCourseQnaCommand command) {
        log.info("[Course Q&A Command] Q&A 답변 등록 요청. courseId={}, qnaId={}, managerId={}",
                command.courseId(), command.qnaId(), command.managerId());

        validateCourse(command.courseId());

        CourseQna qna = findQna(command.courseId(), command.qnaId());

        if ("ANSWERED".equals(qna.getStatus())) {
            log.warn("[Course Q&A Command] Q&A 답변 실패. 이미 답변이 등록되었습니다. courseId={}, qnaId={}",
                    command.courseId(), command.qnaId());
            throw new LmsException(LmsErrorCode.QNA_ALREADY_ANSWERED);
        }

        CourseQna answeredQna = qna.answer(
                command.managerId(),
                command.answer()
        );

        CourseQna savedQna = courseQnaRepository.save(answeredQna);

        log.info("[Course Q&A Command] Q&A 답변 등록 완료. qnaId={}, managerId={}",
                savedQna.getId(), savedQna.getManagerId());

        return savedQna;
    }

    @Override
    @Transactional
    public CourseQnaComment createComment(CreateCourseQnaCommentCommand command) {
        log.info("[Course Q&A Command] Q&A 댓글 등록 요청. courseId={}, qnaId={}, writerId={}, writerType={}",
                command.courseId(), command.qnaId(), command.writerId(), command.writerType());

        validateCourse(command.courseId());
        findQna(command.courseId(), command.qnaId());

        CourseQnaComment comment;

        if ("MANAGER".equals(command.writerType())) {
            comment = CourseQnaComment.createManagerComment(
                    command.qnaId(),
                    command.writerId(),
                    command.content()
            );
        } else {
            comment = CourseQnaComment.createUserComment(
                    command.qnaId(),
                    command.writerId(),
                    command.content()
            );
        }

        CourseQnaComment savedComment = courseQnaCommentRepository.save(comment);

        log.info("[Course Q&A Command] Q&A 댓글 등록 완료. commentId={}, qnaId={}, writerType={}",
                savedComment.getId(), savedComment.getQnaId(), savedComment.getWriterType());

        return savedComment;
    }

    private void validateCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            log.warn("[Course Q&A] Q&A 처리 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}", courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private CourseQna findQna(
            Long courseId,
            Long qnaId
    ) {
        return courseQnaRepository.findByIdAndCourseId(qnaId, courseId)
                .orElseThrow(() -> {
                    log.warn("[Course Q&A] Q&A 처리 실패. Q&A를 찾을 수 없습니다. courseId={}, qnaId={}",
                            courseId, qnaId);
                    return new LmsException(LmsErrorCode.QNA_NOT_FOUND);
                });
    }
}