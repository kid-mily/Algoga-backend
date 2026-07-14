package com.kidmily.algoga_server.quiz.application.service;

// [리팩토링] 수료 로직을 CourseCompletionRegistrar로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.global.event.CourseCompletionCompletedEvent;
import com.kidmily.algoga_server.quiz.application.command.CreateQuizCommand;
import com.kidmily.algoga_server.quiz.application.command.SubmitQuizAnswerCommand;
import com.kidmily.algoga_server.quiz.application.command.SubmitQuizCommand;
import com.kidmily.algoga_server.quiz.application.command.UpdateQuizCommand;
// [리팩토링] 검증 로직을 QuizAccessPolicy로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.learningprogress.application.port.LearningProgressCachePort;
import com.kidmily.algoga_server.completion.application.result.CourseCompletionResult;
import com.kidmily.algoga_server.completion.application.service.CourseCompletionRegistrar;
import com.kidmily.algoga_server.quiz.application.result.QuizResult;
import com.kidmily.algoga_server.quiz.application.result.QuizSubmissionAnswerResult;
import com.kidmily.algoga_server.quiz.application.result.QuizSubmissionResult;
import com.kidmily.algoga_server.quiz.application.result.QuizSubmitResult;
// [리팩토링] 채점 로직을 QuizGrader로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.quiz.application.result.WrongQuizAnswerResult;
import com.kidmily.algoga_server.quiz.application.usecase.QuizUseCase;
import com.kidmily.algoga_server.quiz.application.policy.QuizAccessPolicy;
// [리팩토링] 검증 로직을 QuizAccessPolicy로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.course.domain.model.Chapter;
// [리팩토링] 수료 로직을 CourseCompletionRegistrar로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
//import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.quiz.domain.model.Quiz;
import com.kidmily.algoga_server.quiz.domain.model.QuizSubmission;
import com.kidmily.algoga_server.quiz.domain.model.QuizSubmissionAnswer;
// [리팩토링] 검증 로직을 QuizAccessPolicy로 이관하여 미사용이 된 import들. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.course.domain.repository.ChapterRepository;
// [리팩토링] 수료 로직을 CourseCompletionRegistrar로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
//import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
//import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
//import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionAnswerRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;
import lombok.RequiredArgsConstructor;
// [리팩토링] 수료 로직을 CourseCompletionRegistrar로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [리팩토링] 검증 로직을 QuizAccessPolicy로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import java.time.LocalDateTime;
// [리팩토링] 채점 로직을 QuizGrader로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import java.util.ArrayList;
//import java.util.HashSet;
import java.util.List;
import java.util.Map;
// [리팩토링] 수료 로직을 CourseCompletionRegistrar로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import java.util.Optional;
// [리팩토링] 채점 로직을 QuizGrader로 이관하여 미사용이 된 import. 삭제 대신 이력 보존용으로 주석 처리함.
//import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService implements QuizUseCase {

    // [리팩토링] 코스/수강/챕터완료 검증을 QuizAccessPolicy로 이관하여 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
    //private final CourseRepository courseRepository;
    //private final ChapterRepository chapterRepository;
    //private final LearningProgressRepository learningProgressRepository;
    //private final LearningProgressCachePort learningProgressCachePort;
    //private final EnrollmentRepository enrollmentRepository;
    private final QuizAccessPolicy quizAccessPolicy;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizSubmissionAnswerRepository quizSubmissionAnswerRepository;
    // [리팩토링] 수료 생성/이벤트 로직을 CourseCompletionRegistrar로 이관하여 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
    //private final CourseCompletionRepository courseCompletionRepository;
    //private final ApplicationEventPublisher eventPublisher;
    private final CourseCompletionRegistrar courseCompletionRegistrar;

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
            throw new LearningException(LearningErrorCode.QUIZ_NOT_FOUND);
        }

        return quizzes.stream()
                .map(QuizResult::from)
                .toList();
    }

    @Override
    public QuizResult createQuiz(CreateQuizCommand command) {
        quizAccessPolicy.validateActiveCourse(command.courseId());
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
        ).orElseThrow(() -> new LearningException(LearningErrorCode.QUIZ_NOT_FOUND));

        return QuizResult.from(updatedQuiz);
    }

    @Override
    public void deleteQuiz(Long courseId, Long quizId) {
        quizAccessPolicy.validateActiveCourse(courseId);

        if (!quizRepository.delete(quizId, courseId)) {
            throw new LearningException(LearningErrorCode.QUIZ_NOT_FOUND);
        }
    }

    @Override
    public QuizSubmitResult submitQuiz(SubmitQuizCommand command) {
        quizAccessPolicy.validateEnrollment(command.userId(), command.courseId());
        quizAccessPolicy.validateCourseExists(command.courseId());
        quizAccessPolicy.validateAllChaptersCompleted(command.userId(), command.courseId());

        List<Quiz> quizzes = quizRepository.findByCourseId(command.courseId());
        if (quizzes.isEmpty()) {
            throw new LearningException(LearningErrorCode.QUIZ_NOT_FOUND);
        }

        Map<Long, Quiz> quizMap = quizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, Function.identity()));

        // [리팩토링] 제출 검증/채점/점수계산을 QuizGrader로 이관. 아래 원본 채점 코드는 삭제 대신 이력 보존용으로 주석 처리함.
        /*
        validateSubmission(command.answers(), quizMap, quizzes.size());

        int correctCount = 0;
        List<WrongQuizAnswerResult> wrongAnswers = new ArrayList<>();

        for (SubmitQuizAnswerCommand answer : command.answers()) {
            Quiz quiz = quizMap.get(answer.quizId());
            boolean correct = quiz.getCorrectOption() == answer.selectedOption();

            if (correct) {
                correctCount++;
            } else {
                wrongAnswers.add(new WrongQuizAnswerResult(
                        quiz.getId(),
                        quiz.getQuestion(),
                        answer.selectedOption(),
                        quiz.getCorrectOption(),
                        quiz.getExplanation()
                ));
            }
        }

        int score = calculateScore(correctCount, quizzes.size());
        */
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
                .orElseThrow(() -> new LearningException(LearningErrorCode.QUIZ_NOT_SUBMITTED));

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

    // [리팩토링] 수료 생성/이벤트 로직을 CourseCompletionRegistrar로 이관하여 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
    /*
    private CourseCompletionResult completeCourseIfNeeded(Long userId, Long courseId) {
        Optional<CourseCompletion> existingCompletion =
                courseCompletionRepository.findByUserIdAndCourseId(userId, courseId);

        if (existingCompletion.isPresent()) {
            return CourseCompletionResult.from(existingCompletion.get());
        }

        CourseCompletion savedCompletion = courseCompletionRepository.save(
                CourseCompletion.create(userId, courseId)
        );

        eventPublisher.publishEvent(new CourseCompletionCompletedEvent(
                savedCompletion.getUserId(),
                savedCompletion.getCourseId(),
                savedCompletion.getId(),
                savedCompletion.getCompletedAt()
        ));

        return CourseCompletionResult.from(savedCompletion);
    }
    */

    // [리팩토링] 코스/수강/챕터완료 검증을 QuizAccessPolicy로 이관하여 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
    /*
    private void validateActiveCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            throw new LearningException(LearningErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateCourseExists(Long courseId) {
        if (courseRepository.findById(courseId).isEmpty()) {
            throw new LearningException(LearningErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateEnrollment(Long userId, Long courseId) {
        boolean accessible = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);

        if (!accessible) {
            throw new LearningException(LearningErrorCode.NOT_ENROLLED);
        }
    }

    private void validateAllChaptersCompleted(Long userId, Long courseId) {
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        if (chapters.isEmpty()) {
            throw new LearningException(LearningErrorCode.QUIZ_LOCKED);
        }

        List<Long> incompleteChapterIds = chapters.stream()
                .filter(chapter -> !isChapterCompleted(
                        userId,
                        courseId,
                        chapter.getId()
                ))
                .map(Chapter::getId)
                .toList();

        if (!incompleteChapterIds.isEmpty()) {
            throw new LearningException(LearningErrorCode.QUIZ_LOCKED);
        }
    }

    private boolean isChapterCompleted(Long userId, Long courseId, Long chapterId) {
        Optional<LearningProgress> cachedProgress = findCachedProgress(userId, courseId, chapterId);

        if (cachedProgress.map(LearningProgress::isCompleted).orElse(false)) {
            return true;
        }

        return learningProgressRepository.existsCompletedByUserIdAndChapterId(userId, chapterId);
    }

    private Optional<LearningProgress> findCachedProgress(Long userId, Long courseId, Long chapterId) {
        try {
            return learningProgressCachePort.find(userId, courseId, chapterId);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }
    */

    // [리팩토링] 입력 검증을 QuizInputValidator로 이관하여 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
    /*
    private void validateOptions(String option1, String option2, String option3, String option4) {
        if (isBlank(option1) || isBlank(option2) || isBlank(option3) || isBlank(option4)) {
            throw new LearningException(LearningErrorCode.INVALID_QUIZ_OPTION);
        }
    }

    private void validateCorrectOption(int correctOption) {
        if (correctOption < 1 || correctOption > 4) {
            throw new LearningException(LearningErrorCode.INVALID_QUIZ_ANSWER);
        }
    }
    */

    // [리팩토링] 제출 검증/점수계산을 QuizGrader로 이관하여 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
    /*
    private void validateSubmission(List<SubmitQuizAnswerCommand> answers, Map<Long, Quiz> quizMap, int quizCount) {
        if (answers == null || answers.size() != quizCount) {
            throw new LearningException(LearningErrorCode.INVALID_QUIZ_SUBMISSION);
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
                throw new LearningException(LearningErrorCode.INVALID_QUIZ_SUBMISSION);
            }
        }
    }

    private int calculateScore(int correctCount, int totalCount) {
        if (totalCount <= 0) {
            return 0;
        }

        return (int) Math.round((correctCount * 100.0) / totalCount);
    }
    */

    // [리팩토링] 입력 검증을 QuizInputValidator로 이관하여 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
    /*
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
    */
}
