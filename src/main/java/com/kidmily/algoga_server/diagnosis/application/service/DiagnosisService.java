package com.kidmily.algoga_server.diagnosis.application.service;

import com.kidmily.algoga_server.diagnosis.application.command.CreateDiagnosisQuestionCommand;
import com.kidmily.algoga_server.diagnosis.application.command.SubmitDiagnosisAnswerCommand;
import com.kidmily.algoga_server.diagnosis.application.command.SubmitDiagnosisCommand;
import com.kidmily.algoga_server.diagnosis.application.command.UpdateDiagnosisQuestionCommand;
import com.kidmily.algoga_server.lms.application.port.UserProfilePort;
import com.kidmily.algoga_server.diagnosis.application.result.AdminDiagnosisResult;
import com.kidmily.algoga_server.diagnosis.application.result.DiagnosisAnswerResult;
import com.kidmily.algoga_server.diagnosis.application.result.DiagnosisQuestionResult;
import com.kidmily.algoga_server.diagnosis.application.result.DiagnosisResultView;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.diagnosis.application.usecase.DiagnosisUseCase;
import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisAnswer;
import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisQuestion;
import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisResult;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisAnswerRepository;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisQuestionRepository;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisResultRepository;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kidmily.algoga_server.country.domain.model.Country;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosisService implements DiagnosisUseCase {

    private final DiagnosisQuestionRepository diagnosisQuestionRepository;
    private final DiagnosisResultRepository diagnosisResultRepository;
    private final DiagnosisAnswerRepository diagnosisAnswerRepository;
    private final MapRepository mapRepository;
    private final CourseUseCase courseUseCase;
    private final UserProfilePort userProfilePort;

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisQuestionResult> getQuestions(Long countryId) {
        validateCountry(countryId);
        return diagnosisQuestionRepository.findActiveByCountryId(countryId).stream()
                .map(DiagnosisQuestionResult::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisQuestionResult> getAdminQuestions(Long countryId) {
        validateCountry(countryId);
        return diagnosisQuestionRepository.findByCountryId(countryId).stream()
                .map(DiagnosisQuestionResult::from)
                .toList();
    }

    @Override
    public DiagnosisQuestionResult createQuestion(CreateDiagnosisQuestionCommand command) {
        validateCountry(command.countryId());
        validateDiagnosisQuestion(command.correctOption(), command.questionOrder());

        DiagnosisQuestion question = new DiagnosisQuestion(
                null,
                command.countryId(),
                command.questionText(),
                command.option1(),
                command.option2(),
                command.option3(),
                command.option4(),
                command.correctOption(),
                command.explanation(),
                command.questionOrder(),
                command.active() == null || command.active()
        );

        return DiagnosisQuestionResult.from(diagnosisQuestionRepository.save(question));
    }

    @Override
    public DiagnosisQuestionResult updateQuestion(Long questionId, UpdateDiagnosisQuestionCommand command) {
        DiagnosisQuestion existingQuestion = diagnosisQuestionRepository.findById(questionId)
                .orElseThrow(() -> new LmsException(LmsErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND));

        validateDiagnosisQuestion(command.correctOption(), command.questionOrder());

        DiagnosisQuestion question = new DiagnosisQuestion(
                existingQuestion.id(),
                existingQuestion.countryId(),
                command.questionText(),
                command.option1(),
                command.option2(),
                command.option3(),
                command.option4(),
                command.correctOption(),
                command.explanation(),
                command.questionOrder(),
                command.active() == null || command.active()
        );

        return DiagnosisQuestionResult.from(diagnosisQuestionRepository.save(question));
    }

    @Override
    public DiagnosisResultView submitResult(SubmitDiagnosisCommand command) {
        validateCountry(command.countryId());
        validateDuplicateAnswers(command.answers());

        List<Long> questionIds = command.answers()
                .stream()
                .map(SubmitDiagnosisAnswerCommand::questionId)
                .toList();

        Map<Long, DiagnosisQuestion> questionMap = diagnosisQuestionRepository.findByIds(questionIds)
                .stream()
                .collect(Collectors.toMap(DiagnosisQuestion::id, Function.identity()));

        if (questionMap.size() != questionIds.size()) {
            throw new LmsException(LmsErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND);
        }

        int correctCount = 0;

        for (SubmitDiagnosisAnswerCommand answer : command.answers()) {
            DiagnosisQuestion question = questionMap.get(answer.questionId());

            if (!question.active() || !question.countryId().equals(command.countryId())) {
                throw new LmsException(LmsErrorCode.INVALID_DIAGNOSIS_ANSWER);
            }

            if (question.correctOption() == answer.selectedOption()) {
                correctCount++;
            }
        }

        int totalCount = command.answers().size();
        int score = calculateScore(correctCount, totalCount);
        String level = calculateLevel(score);

        DiagnosisResult savedResult = diagnosisResultRepository.save(new DiagnosisResult(
                null,
                command.userId(),
                command.countryId(),
                correctCount,
                totalCount,
                score,
                level,
                null
        ));

        List<DiagnosisAnswerResult> answerResults = command.answers()
                .stream()
                .map(answer -> saveAndCreateAnswerResult(savedResult.id(), answer, questionMap.get(answer.questionId())))
                .toList();

        if (userProfilePort.findProfile(command.userId()).isEmpty()) {
            throw new LmsException(LmsErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }

        userProfilePort.updateDiagnosisResult(command.userId(), command.countryId(), level, score);

        return toResultView(savedResult, answerResults);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisResultView> getLatestResultsByCountry(Long userId) {
        List<DiagnosisResult> results = diagnosisResultRepository.findLatestResultsByCountry(userId);

        if (results.isEmpty()) {
            throw new LmsException(LmsErrorCode.DIAGNOSIS_RESULT_NOT_FOUND);
        }

        return results.stream()
                .map(result -> toResultView(result, List.of()))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AdminDiagnosisResult> getAdminResults(Long userId, Long countryId) {
        if (countryId != null) {
            validateCountry(countryId);
        }

        return diagnosisResultRepository.findForAdmin(userId, countryId).stream()
                .map(AdminDiagnosisResult::from)
                .toList();
    }

    @Override
    public void deleteQuestion(Long questionId) {
        if (!diagnosisQuestionRepository.existsById(questionId)) {
            throw new LmsException(LmsErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND);
        }

        diagnosisAnswerRepository.deleteByQuestionId(questionId);
        diagnosisQuestionRepository.deleteById(questionId);
    }

    private DiagnosisAnswerResult saveAndCreateAnswerResult(
            Long resultId,
            SubmitDiagnosisAnswerCommand answer,
            DiagnosisQuestion question
    ) {
        boolean correct = question.correctOption() == answer.selectedOption();

        diagnosisAnswerRepository.save(DiagnosisAnswer.create(
                resultId,
                question.id(),
                answer.selectedOption(),
                correct
        ));

        return new DiagnosisAnswerResult(
                question.id(),
                answer.selectedOption(),
                question.correctOption(),
                correct,
                question.explanation()
        );
    }

    private DiagnosisResultView toResultView(
            DiagnosisResult result,
            List<DiagnosisAnswerResult> answerResults
    ) {
        return new DiagnosisResultView(
                result.id(),
                result.countryId(),
                findCountryName(result.countryId()),
                result.correctCount(),
                result.totalCount(),
                result.score(),
                result.level(),
                toLevelName(result.level()),
                result.createdAt(),
                answerResults,
                courseUseCase.getRecommendedCoursesByCountryAndLevel(result.countryId(), result.level())
        );
    }

    private void validateCountry(Long countryId) {
        if (mapRepository.findActiveCountryById(countryId).isEmpty()) {
            throw new LmsException(LmsErrorCode.COUNTRY_NOT_FOUND);
        }
    }

    private String findCountryName(Long countryId) {
        return mapRepository.findActiveCountryById(countryId)
                .map(Country::getName)
                .orElse(null);
    }

    private void validateDiagnosisQuestion(Integer correctOption, Integer questionOrder) {
        if (correctOption == null || correctOption < 1 || correctOption > 4) {
            throw new LmsException(LmsErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }

        if (questionOrder == null || questionOrder < 1) {
            throw new LmsException(LmsErrorCode.INVALID_DIAGNOSIS_ANSWER);
        }
    }

    private void validateDuplicateAnswers(List<SubmitDiagnosisAnswerCommand> answers) {
        LinkedHashSet<Long> uniqueQuestionIds = answers.stream()
                .map(SubmitDiagnosisAnswerCommand::questionId)
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
