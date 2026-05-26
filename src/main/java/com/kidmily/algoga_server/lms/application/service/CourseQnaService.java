package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseQnaUseCase;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;
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
    @Transactional
    public CourseQna answerQna(AnswerCourseQnaCommand command) {
        log.info("[Course Q&A Command] Q&A 답변 등록 요청. courseId={}, qnaId={}, managerId={}",
                command.courseId(), command.qnaId(), command.managerId());

        validateCourse(command.courseId());

        CourseQna qna = courseQnaRepository.findByIdAndCourseId(
                command.qnaId(),
                command.courseId()
        ).orElseThrow(() -> {
            log.warn("[Course Q&A Command] Q&A 답변 실패. Q&A를 찾을 수 없습니다. courseId={}, qnaId={}",
                    command.courseId(), command.qnaId());
            return new LmsException(LmsErrorCode.QNA_NOT_FOUND);
        });

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

    private void validateCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            log.warn("[Course Q&A] Q&A 처리 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}", courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }
}