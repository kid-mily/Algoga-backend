package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.CreateQuizCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateQuizCommand;
import com.kidmily.algoga_server.lms.application.usecase.AdminQuizUseCase;
import com.kidmily.algoga_server.lms.domain.model.Quiz;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizRepository;
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
@Transactional
public class AdminQuizService implements AdminQuizUseCase {

    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getQuizzes(Long courseId) {
        log.info("[Quiz Query] 어드민 퀴즈 목록 조회 요청. courseId={}", courseId);

        validateCourse(courseId, "퀴즈 목록 조회");

        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);

        log.info("[Quiz Query] 어드민 퀴즈 목록 조회 완료. courseId={}, count={}",
                courseId, quizzes.size());

        return quizzes;
    }

    @Override
    public Quiz createQuiz(CreateQuizCommand command) {
        log.info("[Quiz Command] 퀴즈 등록 요청. courseId={}, question={}",
                command.courseId(), command.question());

        validateCourse(command.courseId(), "퀴즈 등록");
        validateOptions(command.option1(), command.option2(), command.option3(), command.option4());
        validateCorrectOption(command.correctOption());

        Quiz quiz = Quiz.create(
                command.courseId(),
                command.question(),
                command.option1(),
                command.option2(),
                command.option3(),
                command.option4(),
                command.correctOption(),
                command.explanation()
        );

        Quiz savedQuiz = quizRepository.save(quiz);

        log.info("[Quiz Command] 퀴즈 등록 완료. courseId={}, quizId={}",
                savedQuiz.getCourseId(), savedQuiz.getId());

        return savedQuiz;
    }

    @Override
    public Quiz updateQuiz(
            Long courseId,
            Long quizId,
            UpdateQuizCommand command
    ) {
        log.info("[Quiz Command] 퀴즈 수정 요청. courseId={}, quizId={}, question={}",
                courseId, quizId, command.question());

        validateCourse(courseId, "퀴즈 수정");
        validateOptions(command.option1(), command.option2(), command.option3(), command.option4());
        validateCorrectOption(command.correctOption());

        Quiz updatedQuiz = quizRepository.updateBasicInfo(
                quizId,
                courseId,
                command.question(),
                command.option1(),
                command.option2(),
                command.option3(),
                command.option4(),
                command.correctOption(),
                command.explanation()
        ).orElseThrow(() -> {
            log.warn("[Quiz Command] 퀴즈 수정 실패. 존재하지 않거나 삭제된 퀴즈입니다. courseId={}, quizId={}",
                    courseId, quizId);
            return new LmsException(LmsErrorCode.QUIZ_NOT_FOUND);
        });

        log.info("[Quiz Command] 퀴즈 수정 완료. courseId={}, quizId={}",
                courseId, updatedQuiz.getId());

        return updatedQuiz;
    }

    @Override
    public void deleteQuiz(Long courseId, Long quizId) {
        log.info("[Quiz Command] 퀴즈 삭제 요청. courseId={}, quizId={}",
                courseId, quizId);

        validateCourse(courseId, "퀴즈 삭제");

        boolean deleted = quizRepository.softDelete(quizId, courseId);

        if (!deleted) {
            log.warn("[Quiz Command] 퀴즈 삭제 실패. 존재하지 않거나 이미 삭제된 퀴즈입니다. courseId={}, quizId={}",
                    courseId, quizId);
            throw new LmsException(LmsErrorCode.QUIZ_NOT_FOUND);
        }

        log.info("[Quiz Command] 퀴즈 삭제 완료. courseId={}, quizId={}",
                courseId, quizId);
    }

    private void validateCourse(Long courseId, String action) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            log.warn("[Quiz Command] {} 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}",
                    action, courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateOptions(
            String option1,
            String option2,
            String option3,
            String option4
    ) {
        if (isBlank(option1) || isBlank(option2) || isBlank(option3) || isBlank(option4)) {
            log.warn("[Quiz Command] 퀴즈 보기 검증 실패. 4개 보기가 모두 필요합니다.");
            throw new LmsException(LmsErrorCode.INVALID_QUIZ_OPTION);
        }
    }

    private void validateCorrectOption(int correctOption) {
        if (correctOption < 1 || correctOption > 4) {
            log.warn("[Quiz Command] 퀴즈 정답 번호 검증 실패. correctOption={}", correctOption);
            throw new LmsException(LmsErrorCode.INVALID_QUIZ_ANSWER);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}