package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.CreateQuizCommand;
import com.kidmily.algoga_server.lms.application.command.SubmitQuizAnswerCommand;
import com.kidmily.algoga_server.lms.application.command.SubmitQuizCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateQuizCommand;
import com.kidmily.algoga_server.lms.application.result.QuizResult;
import com.kidmily.algoga_server.lms.application.result.QuizSubmitResult;
import com.kidmily.algoga_server.lms.application.result.WrongQuizAnswerResult;
import com.kidmily.algoga_server.lms.application.usecase.QuizUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Quiz;
import com.kidmily.algoga_server.lms.domain.model.QuizSubmission;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService implements QuizUseCase {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<QuizResult> getQuizzes(Long courseId) {
        validateActiveCourse(courseId);
        return quizRepository.findByCourseId(courseId).stream()
                .map(QuizResult::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResult> getQuizzes(Long userId, Long courseId) {
        validateEnrollment(userId, courseId);
        validateCourseExists(courseId);
        validateAllChaptersCompleted(userId, courseId);

        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        if (quizzes.isEmpty()) {
            throw new LmsException(LmsErrorCode.QUIZ_NOT_FOUND);
        }

        return quizzes.stream()
                .map(QuizResult::from)
                .toList();
    }

    @Override
    public QuizResult createQuiz(CreateQuizCommand command) {
        validateActiveCourse(command.courseId());
        validateOptions(command.option1(), command.option2(), command.option3(), command.option4());
        validateCorrectOption(command.correctOption());

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
        validateActiveCourse(courseId);
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
        ).orElseThrow(() -> new LmsException(LmsErrorCode.QUIZ_NOT_FOUND));

        return QuizResult.from(updatedQuiz);
    }

    @Override
    public void deleteQuiz(Long courseId, Long quizId) {
        validateActiveCourse(courseId);

        if (!quizRepository.delete(quizId, courseId)) {
            throw new LmsException(LmsErrorCode.QUIZ_NOT_FOUND);
        }
    }

    @Override
    public QuizSubmitResult submitQuiz(SubmitQuizCommand command) {
        validateEnrollment(command.userId(), command.courseId());
        validateCourseExists(command.courseId());
        validateAllChaptersCompleted(command.userId(), command.courseId());

        List<Quiz> quizzes = quizRepository.findByCourseId(command.courseId());
        if (quizzes.isEmpty()) {
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

        quizSubmissionRepository.save(QuizSubmission.create(
                command.userId(),
                command.courseId(),
                quizzes.size(),
                correctCount,
                score
        ));

        return new QuizSubmitResult(
                command.userId(),
                command.courseId(),
                quizzes.size(),
                correctCount,
                score,
                wrongAnswers
        );
    }

    private void validateActiveCourse(Long courseId) {
        if (courseRepository.findByIdAndDeletedFalse(courseId).isEmpty()) {
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateCourseExists(Long courseId) {
        if (courseRepository.findById(courseId).isEmpty()) {
            throw new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
        }
    }

    private void validateEnrollment(Long userId, Long courseId) {
        boolean accessible = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);

        if (!accessible) {
            throw new LmsException(LmsErrorCode.NOT_ENROLLED);
        }
    }

    private void validateAllChaptersCompleted(Long userId, Long courseId) {
        List<Chapter> chapters = chapterRepository.findByCourseId(courseId);

        if (chapters.isEmpty()) {
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
            throw new LmsException(LmsErrorCode.QUIZ_LOCKED);
        }
    }

    private void validateOptions(String option1, String option2, String option3, String option4) {
        if (isBlank(option1) || isBlank(option2) || isBlank(option3) || isBlank(option4)) {
            throw new LmsException(LmsErrorCode.INVALID_QUIZ_OPTION);
        }
    }

    private void validateCorrectOption(int correctOption) {
        if (correctOption < 1 || correctOption > 4) {
            throw new LmsException(LmsErrorCode.INVALID_QUIZ_ANSWER);
        }
    }

    private void validateSubmission(List<SubmitQuizAnswerCommand> answers, Map<Long, Quiz> quizMap, int quizCount) {
        if (answers == null || answers.size() != quizCount) {
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
                throw new LmsException(LmsErrorCode.INVALID_QUIZ_SUBMISSION);
            }
        }
    }

    private int calculateScore(int correctCount, int totalCount) {
        if (totalCount <= 0) {
            return 0;
        }

        return (int) Math.round((correctCount * 100.0) / totalCount);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
