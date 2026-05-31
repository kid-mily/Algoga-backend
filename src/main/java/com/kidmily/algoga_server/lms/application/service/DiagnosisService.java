package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisAnswerJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisQuestionJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.DiagnosisResultJpaEntity;
import com.kidmily.algoga_server.lms.tdd.SpringDataDiagnosisAnswerRepository;
import com.kidmily.algoga_server.lms.tdd.SpringDataDiagnosisQuestionRepository;
import com.kidmily.algoga_server.lms.tdd.SpringDataDiagnosisResultRepository;
import com.kidmily.algoga_server.lms.presentation.request.DiagnosisAnswerRequest;
import com.kidmily.algoga_server.lms.presentation.request.DiagnosisSubmitRequest;
import com.kidmily.algoga_server.lms.presentation.response.CourseListResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisAnswerResultResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisQuestionResponse;
import com.kidmily.algoga_server.lms.presentation.response.DiagnosisResultResponse;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosisService {

    private final SpringDataDiagnosisQuestionRepository diagnosisQuestionRepository;
    private final SpringDataDiagnosisResultRepository diagnosisResultRepository;
    private final SpringDataDiagnosisAnswerRepository diagnosisAnswerRepository;
    private final MapRepository mapRepository;
    private final CourseUseCase courseUseCase;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DiagnosisQuestionResponse> getQuestions(Long countryId) {
        validateCountry(countryId);

        return diagnosisQuestionRepository
                .findByCountryIdAndActiveTrueOrderByQuestionOrderAscIdAsc(countryId)
                .stream()
                .map(DiagnosisQuestionResponse::from)
                .toList();
    }

    public DiagnosisResultResponse submitResult(Long userId, DiagnosisSubmitRequest request) {
        validateCountry(request.countryId());
        validateDuplicateAnswers(request.answers());

        List<Long> questionIds = request.answers()
                .stream()
                .map(DiagnosisAnswerRequest::questionId)
                .toList();

        Map<Long, DiagnosisQuestionJpaEntity> questionMap = diagnosisQuestionRepository.findByIdIn(questionIds)
                .stream()
                .collect(Collectors.toMap(DiagnosisQuestionJpaEntity::getId, Function.identity()));

        if (questionMap.size() != questionIds.size()) {
            throw new LmsException(LmsErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND);
        }

        int correctCount = 0;

        for (DiagnosisAnswerRequest answer : request.answers()) {
            DiagnosisQuestionJpaEntity question = questionMap.get(answer.questionId());

            if (!question.isActive() || !question.getCountryId().equals(request.countryId())) {
                throw new LmsException(LmsErrorCode.INVALID_DIAGNOSIS_ANSWER);
            }

            if (question.getCorrectOption() == answer.selectedOption()) {
                correctCount++;
            }
        }

        int totalCount = request.answers().size();
        int score = calculateScore(correctCount, totalCount);
        String level = calculateLevel(score);

        DiagnosisResultJpaEntity result = diagnosisResultRepository.save(
                new DiagnosisResultJpaEntity(
                        userId,
                        request.countryId(),
                        correctCount,
                        totalCount,
                        score,
                        level
                )
        );

        List<DiagnosisAnswerResultResponse> answerResults = request.answers()
                .stream()
                .map(answer -> {
                    DiagnosisQuestionJpaEntity question = questionMap.get(answer.questionId());
                    boolean correct = question.getCorrectOption() == answer.selectedOption();

                    diagnosisAnswerRepository.save(
                            new DiagnosisAnswerJpaEntity(
                                    result.getId(),
                                    question.getId(),
                                    answer.selectedOption(),
                                    correct
                            )
                    );

                    return new DiagnosisAnswerResultResponse(
                            question.getId(),
                            answer.selectedOption(),
                            question.getCorrectOption(),
                            correct,
                            question.getExplanation()
                    );
                })
                .toList();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.INVALID_DIAGNOSIS_ANSWER));

        user.updateDiagnosisResult(request.countryId(), level, score);

        List<CourseListResponse> recommendedCourses = courseUseCase
                .getRecommendedCoursesByCountryAndLevel(request.countryId(), level)
                .stream()
                .map(CourseListResponse::from)
                .toList();

        return new DiagnosisResultResponse(
                result.getId(),
                request.countryId(),
                correctCount,
                totalCount,
                score,
                level,
                toLevelName(level),
                result.getCreatedAt(),
                answerResults,
                recommendedCourses
        );
    }

    @Transactional(readOnly = true)
    public DiagnosisResultResponse getLatestResult(Long userId) {
        DiagnosisResultJpaEntity result = diagnosisResultRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.DIAGNOSIS_RESULT_NOT_FOUND));

        List<CourseListResponse> recommendedCourses = courseUseCase
                .getRecommendedCoursesByCountryAndLevel(result.getCountryId(), result.getLevel())
                .stream()
                .map(CourseListResponse::from)
                .toList();

        return new DiagnosisResultResponse(
                result.getId(),
                result.getCountryId(),
                result.getCorrectCount(),
                result.getTotalCount(),
                result.getScore(),
                result.getLevel(),
                toLevelName(result.getLevel()),
                result.getCreatedAt(),
                List.of(),
                recommendedCourses
        );
    }

    private void validateCountry(Long countryId) {
        if (mapRepository.findActiveCountryById(countryId).isEmpty()) {
            throw new LmsException(LmsErrorCode.COUNTRY_NOT_FOUND);
        }
    }

    private void validateDuplicateAnswers(List<DiagnosisAnswerRequest> answers) {
        LinkedHashSet<Long> uniqueQuestionIds = answers.stream()
                .map(DiagnosisAnswerRequest::questionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (uniqueQuestionIds.size() != answers.size()) {
            throw new LmsException(LmsErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }
    }

    private int calculateScore(int correctCount, int totalCount) {
        return correctCount * 100 / totalCount;
    }

    private String calculateLevel(int score) {
        if (score <= 40) {
            return "BEGINNER";
        }

        if (score <= 70) {
            return "INTERMEDIATE";
        }

        return "ADVANCED";
    }

    private String toLevelName(String level) {
        return switch (level) {
            case "BEGINNER" -> "초급";
            case "INTERMEDIATE" -> "중급";
            case "ADVANCED" -> "고급";
            default -> "";
        };
    }
}