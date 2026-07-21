package com.kidmily.algoga_server.diagnosis.application.service;

import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.course.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.diagnosis.application.command.SubmitDiagnosisAnswerCommand;
import com.kidmily.algoga_server.diagnosis.application.command.SubmitDiagnosisCommand;
import com.kidmily.algoga_server.diagnosis.domain.model.DiagnosisQuestion;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisAnswerRepository;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisQuestionRepository;
import com.kidmily.algoga_server.diagnosis.domain.repository.DiagnosisResultRepository;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.diagnosis.exception.DiagnosisErrorCode;
import com.kidmily.algoga_server.diagnosis.exception.DiagnosisException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceTest {

    @Mock private DiagnosisQuestionRepository diagnosisQuestionRepository;
    @Mock private DiagnosisResultRepository diagnosisResultRepository;
    @Mock private DiagnosisAnswerRepository diagnosisAnswerRepository;
    @Mock private MapRepository mapRepository;
    @Mock private CourseUseCase courseUseCase;
    @Mock private UserProfilePort userProfilePort;

    @InjectMocks
    private DiagnosisService diagnosisService;

    @Test
    void deletesAnswersBeforeDeletingQuestion() {
        Long questionId = 1L;
        when(diagnosisQuestionRepository.existsById(questionId)).thenReturn(true);

        diagnosisService.deleteQuestion(questionId);

        InOrder inOrder = inOrder(diagnosisAnswerRepository, diagnosisQuestionRepository);
        inOrder.verify(diagnosisAnswerRepository).deleteByQuestionId(questionId);
        inOrder.verify(diagnosisQuestionRepository).deleteById(questionId);
    }

    @Test
    void throwsWhenDiagnosisQuestionDoesNotExist() {
        Long questionId = 1L;
        when(diagnosisQuestionRepository.existsById(questionId)).thenReturn(false);

        DiagnosisException exception = assertThrows(
                DiagnosisException.class,
                () -> diagnosisService.deleteQuestion(questionId)
        );

        assertSame(DiagnosisErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(diagnosisAnswerRepository);
    }

    @Test
    void rejectsSubmissionThatDoesNotCoverAllActiveQuestions() {
        Long countryId = 1L;
        Long userId = 10L;

        when(mapRepository.findActiveCountryById(countryId))
                .thenReturn(java.util.Optional.of(Country.withId(countryId, "KR", "AS", "Asia", "Korea", true)));

        DiagnosisQuestion question1 = new DiagnosisQuestion(1L, countryId, "Q1", "a", "b", "c", "d", 1, "exp", 1, true);
        DiagnosisQuestion question2 = new DiagnosisQuestion(2L, countryId, "Q2", "a", "b", "c", "d", 1, "exp", 2, true);
        when(diagnosisQuestionRepository.findActiveByCountryId(countryId))
                .thenReturn(List.of(question1, question2));

        // 활성 문항은 2개인데 1개만 제출
        SubmitDiagnosisCommand command = new SubmitDiagnosisCommand(
                userId,
                countryId,
                List.of(new SubmitDiagnosisAnswerCommand(1L, 1))
        );

        DiagnosisException exception = assertThrows(
                DiagnosisException.class,
                () -> diagnosisService.submitResult(command)
        );

        assertSame(DiagnosisErrorCode.INVALID_DIAGNOSIS_ANSWER, exception.getErrorCode());
        verifyNoInteractions(diagnosisResultRepository, diagnosisAnswerRepository, userProfilePort);
    }
}
