package com.kidmily.algoga_server.quiz.application.service;

import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.quiz.application.command.CreateQuizCommand;
import com.kidmily.algoga_server.quiz.application.command.SubmitQuizAnswerCommand;
import com.kidmily.algoga_server.quiz.application.command.SubmitQuizCommand;
import com.kidmily.algoga_server.quiz.application.command.UpdateQuizCommand;
import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;
import com.kidmily.algoga_server.completion.application.service.CourseCompletionRegistrar;
import com.kidmily.algoga_server.quiz.application.result.AdminQuizListItemResult;
import com.kidmily.algoga_server.quiz.application.result.QuizResult;
import com.kidmily.algoga_server.quiz.application.result.QuizSubmissionAnswerResult;
import com.kidmily.algoga_server.quiz.application.result.QuizSubmissionResult;
import com.kidmily.algoga_server.quiz.application.result.QuizSubmitResult;
import com.kidmily.algoga_server.quiz.application.usecase.QuizUseCase;
import com.kidmily.algoga_server.quiz.application.policy.QuizAccessPolicy;
import com.kidmily.algoga_server.quiz.domain.model.Quiz;
import com.kidmily.algoga_server.quiz.domain.model.QuizSubmission;
import com.kidmily.algoga_server.quiz.domain.model.QuizSubmissionAnswer;
import com.kidmily.algoga_server.quiz.domain.repository.QuizRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionAnswerRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.quiz.exception.QuizErrorCode;
import com.kidmily.algoga_server.quiz.exception.QuizException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService implements QuizUseCase {

    private static final int MAX_QUIZ_COUNT = 5;

    private final QuizAccessPolicy quizAccessPolicy;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizSubmissionAnswerRepository quizSubmissionAnswerRepository;
    private final CourseCompletionRegistrar courseCompletionRegistrar;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<QuizResult> getQuizzes(Long courseId) {
        quizAccessPolicy.validateActiveCourse(courseId);

        return quizRepository.findByCourseId(courseId).stream()
                .map(QuizResult::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResult> getQuizzes(Long userId, Long courseId) {
        quizAccessPolicy.validateEnrollment(userId, courseId);
        quizAccessPolicy.validateCourseExists(courseId);
        quizAccessPolicy.validateAllChaptersCompleted(userId, courseId);

        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        if (quizzes.isEmpty()) {
            throw new QuizException(QuizErrorCode.QUIZ_NOT_FOUND);
        }

        return quizzes.stream()
                .map(QuizResult::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminQuizListItemResult> getAdminQuizzes(Long courseId, String keyword, Pageable pageable) {
        Page<Quiz> quizzes = quizRepository.searchForAdmin(courseId, keyword, pageable);

        Map<Long, String> courseTitleById = courseRepository.findBasicByIdIn(
                        quizzes.getContent().stream().map(Quiz::getCourseId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(Course::getId, Course::getTitle));

        return quizzes.map(quiz -> AdminQuizListItemResult.from(quiz, courseTitleById.get(quiz.getCourseId())));
    }

    @Override
    public QuizResult createQuiz(CreateQuizCommand command) {
        quizAccessPolicy.validateActiveCourse(command.courseId());
        validateQuizLimit(command.courseId());
        QuizInputValidator.validateOptions(command.option1(), command.option2(), command.option3(), command.option4());
        QuizInputValidator.validateCorrectOption(command.correctOption());

        Quiz savedQuiz = quizRepository.save(Quiz.create(
                command.courseId(),
                command.question(),
                command.option1(),
                command.option2(),
                command.option3(),
                command.option4(),
                command.correctOption(),
                command.explanation()
        ));

        return QuizResult.from(savedQuiz);
    }

    private void validateQuizLimit(Long courseId) {
        if (quizRepository.countByCourseId(courseId) >= MAX_QUIZ_COUNT) {
            throw new QuizException(QuizErrorCode.QUIZ_LIMIT_EXCEEDED);
        }
    }

    @Override
    public QuizResult updateQuiz(Long courseId, Long quizId, UpdateQuizCommand command) {
        quizAccessPolicy.validateActiveCourse(courseId);
        QuizInputValidator.validateOptions(command.option1(), command.option2(), command.option3(), command.option4());
        QuizInputValidator.validateCorrectOption(command.correctOption());

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
        ).orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_FOUND));

        return QuizResult.from(updatedQuiz);
    }

    @Override
    public void deleteQuiz(Long courseId, Long quizId) {
        quizAccessPolicy.validateActiveCourse(courseId);
        validateQuizMinCount(courseId);

        if (!quizRepository.delete(quizId, courseId)) {
            throw new QuizException(QuizErrorCode.QUIZ_NOT_FOUND);
        }
    }

    private void validateQuizMinCount(Long courseId) {
        if (quizRepository.countByCourseId(courseId) <= 1) {
            throw new QuizException(QuizErrorCode.QUIZ_MIN_COUNT_REQUIRED);
        }
    }

    @Override
    public QuizSubmitResult submitQuiz(SubmitQuizCommand command) {
        quizAccessPolicy.validateEnrollment(command.userId(), command.courseId());
        quizAccessPolicy.validateCourseExists(command.courseId());
        quizAccessPolicy.validateAllChaptersCompleted(command.userId(), command.courseId());

        // 퀴즈는 1회만 응시 가능 - 재응시(재제출) 불가
        if (quizSubmissionRepository.existsByUserIdAndCourseId(command.userId(), command.courseId())) {
            throw new QuizException(QuizErrorCode.QUIZ_ALREADY_SUBMITTED);
        }

        List<Quiz> quizzes = quizRepository.findByCourseId(command.courseId());
        if (quizzes.isEmpty()) {
            throw new QuizException(QuizErrorCode.QUIZ_NOT_FOUND);
        }

        Map<Long, Quiz> quizMap = quizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, Function.identity()));

        QuizGrader.Result grading = QuizGrader.grade(command.answers(), quizMap, quizzes.size());

        QuizSubmission savedSubmission = quizSubmissionRepository.save(QuizSubmission.create(
                command.userId(),
                command.courseId(),
                grading.totalCount(),
                grading.correctCount(),
                grading.score()
        ));

        saveSubmissionAnswers(savedSubmission.getId(), command.answers(), quizMap);

        CourseCompletionResult completion = courseCompletionRegistrar.register(
                command.userId(),
                command.courseId()
        );

        return new QuizSubmitResult(
                command.userId(),
                command.courseId(),
                grading.totalCount(),
                grading.correctCount(),
                grading.score(),
                completion != null,
                completion,
                grading.wrongAnswers()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public QuizSubmissionResult getMyQuizSubmission(Long userId, Long courseId) {
        quizAccessPolicy.validateEnrollment(userId, courseId);
        quizAccessPolicy.validateCourseExists(courseId);

        QuizSubmission submission = quizSubmissionRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_SUBMITTED));

        List<QuizSubmissionAnswerResult> answers = quizSubmissionAnswerRepository.findBySubmissionId(submission.getId())
                .stream()
                .map(QuizSubmissionAnswerResult::from)
                .toList();

        return QuizSubmissionResult.from(submission, answers);
    }

    private void saveSubmissionAnswers(
            Long submissionId,
            List<SubmitQuizAnswerCommand> submittedAnswers,
            Map<Long, Quiz> quizMap
    ) {
        quizSubmissionAnswerRepository.deleteBySubmissionId(submissionId);

        List<QuizSubmissionAnswer> answers = submittedAnswers.stream()
                .map(answer -> QuizSubmissionAnswer.create(
                        submissionId,
                        quizMap.get(answer.quizId()),
                        answer.selectedOption()
                ))
                .toList();

        quizSubmissionAnswerRepository.saveAll(answers);
    }

}
