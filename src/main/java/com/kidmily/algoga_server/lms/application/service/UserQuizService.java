package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.SubmitQuizAnswerCommand;
import com.kidmily.algoga_server.lms.application.command.SubmitQuizCommand;
import com.kidmily.algoga_server.lms.application.result.QuizSubmitResult;
import com.kidmily.algoga_server.lms.application.result.WrongQuizAnswerResult;
import com.kidmily.algoga_server.lms.application.usecase.UserQuizUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Quiz;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kidmily.algoga_server.lms.domain.model.QuizSubmission;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQuizService implements UserQuizUseCase {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @Override
    public List<Quiz> getQuizzes(
            Long userId,
            Long courseId
    ) {
        log.info("[User Quiz Query] 사용자 퀴즈 목록 조회 요청. userId={}, courseId={}",
                userId, courseId);

        validateCourse(courseId);
        validateAllChaptersCompleted(userId, courseId);

        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);

        if (quizzes.isEmpty()) {
            log.warn("[User Quiz Query] 퀴즈 목록 조회 실패. 등록된 퀴즈가 없습니다. userId={}, courseId={}",
                    userId, courseId);
            throw new LmsException(LmsErrorCode.QUIZ_NOT_FOUND);
        }

        log.info("[User Quiz Query] 사용자 퀴즈 목록 조회 완료. userId={}, courseId={}, count={}",
                userId, courseId, quizzes.size());

        return quizzes;
    }

    @Override
    @Transactional
    public QuizSubmitResult submitQuiz(SubmitQuizCommand command) {
        log.info("[User Quiz Command] 사용자 퀴즈 제출 요청. userId={}, courseId={}, answerCount={}",
                command.userId(),
                command.courseId(),
                command.answers() == null ? 0 : command.answers().size());

        validateCourse(command.courseId());
        validateAllChaptersCompleted(command.userId(), command.courseId());

        List<Quiz> quizzes = quizRepository.findByCourseId(command.courseId());

        if (quizzes.isEmpty()) {
            log.warn("[User Quiz Command] 퀴즈 제출 실패. 등록된 퀴즈가 없습니다. userId={}, courseId={}",
                    command.userId(), command.courseId());
            throw new LmsException(LmsErrorCode.QUIZ_NOT_FOUND);
        }

        Map<Long, Quiz> quizMap = quizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, Function.identity()));

        validateSubmission(command.answers(), quizMap, quizzes.size());

        int correctCount = 0;
        List<WrongQuizAnswerResult> wrongAnswers = new ArrayList<>();

        for (SubmitQuizAnswerCommand answer : command.answers()) {
            Quiz quiz = quizMap.get(answer.quizId());

            boolean correct = quiz.getCorrectOption() == answer.selectedOption();

            if (correct) {
                correctCount++;
                continue;
            }

            wrongAnswers.add(new WrongQuizAnswerResult(
                    quiz.getId(),
                    quiz.getQuestion(),
                    answer.selectedOption(),
                    quiz.getCorrectOption(),
                    quiz.getExplanation()
            ));
        }

        int score = calculateScore(correctCount, quizzes.size());

        QuizSubmission quizSubmission = QuizSubmission.create(
                command.userId(),
                command.courseId(),
                quizzes.size(),
                correctCount,
                score
        );

        quizSubmissionRepository.save(quizSubmission);

        log.info("[User Quiz Command] 사용자 퀴즈 제출 완료 및 결과 저장. userId={}, courseId={}, totalCount={}, correctCount={}, score={}",
                command.userId(),
                command.courseId(),
                quizzes.size(),
                correctCount,
                score);

        return new QuizSubmitResult(
                command.userId(),
                command.courseId(),
                quizzes.size(),
                correctCount,
                score,
                wrongAnswers
        );
    }

    private void validateCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            log.warn("[User Quiz] 퀴즈 처리 실패. 존재하지 않거나 삭제된 강의입니다. courseId={}",
                    courseId);
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateAllChaptersCompleted(
            Long userId,
            Long courseId
    ) {
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        if (chapters.isEmpty()) {
            log.warn("[User Quiz] 퀴즈 잠금. 강의에 등록된 챕터가 없습니다. userId={}, courseId={}",
                    userId, courseId);
            throw new LmsException(LmsErrorCode.QUIZ_LOCKED);
        }

        List<Long> incompleteChapterIds = chapters.stream()
                .filter(chapter -> !learningProgressRepository.existsCompletedByUserIdAndChapterId(
                        userId,
                        chapter.getId()
                ))
                .map(Chapter::getId)
                .toList();

        if (!incompleteChapterIds.isEmpty()) {
            log.warn("[User Quiz] 퀴즈 잠금. 완료하지 않은 챕터가 있습니다. userId={}, courseId={}, incompleteChapterIds={}",
                    userId, courseId, incompleteChapterIds);
            throw new LmsException(LmsErrorCode.QUIZ_LOCKED);
        }
    }

    private void validateSubmission(
            List<SubmitQuizAnswerCommand> answers,
            Map<Long, Quiz> quizMap,
            int quizCount
    ) {
        if (answers == null || answers.size() != quizCount) {
            log.warn("[User Quiz Command] 퀴즈 제출 답안 검증 실패. 제출 답안 수가 퀴즈 수와 일치하지 않습니다. answerCount={}, quizCount={}",
                    answers == null ? 0 : answers.size(), quizCount);
            throw new LmsException(LmsErrorCode.INVALID_QUIZ_SUBMISSION);
        }

        Set<Long> submittedQuizIds = new HashSet<>();

        for (SubmitQuizAnswerCommand answer : answers) {
            if (answer == null
                    || answer.quizId() == null
                    || answer.selectedOption() == null
                    || answer.selectedOption() < 1
                    || answer.selectedOption() > 4
                    || !quizMap.containsKey(answer.quizId())
                    || !submittedQuizIds.add(answer.quizId())) {

                log.warn("[User Quiz Command] 퀴즈 제출 답안 검증 실패. answer={}",
                        answer);
                throw new LmsException(LmsErrorCode.INVALID_QUIZ_SUBMISSION);
            }
        }
    }

    private int calculateScore(
            int correctCount,
            int totalCount
    ) {
        if (totalCount <= 0) {
            return 0;
        }

        return (int) Math.round((correctCount * 100.0) / totalCount);
    }
}