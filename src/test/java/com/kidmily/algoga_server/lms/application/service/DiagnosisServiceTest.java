package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.port.UserProfilePort;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.domain.repository.DiagnosisAnswerRepository;
import com.kidmily.algoga_server.lms.domain.repository.DiagnosisQuestionRepository;
import com.kidmily.algoga_server.lms.domain.repository.DiagnosisResultRepository;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        LmsException exception = assertThrows(
                LmsException.class,
                () -> diagnosisService.deleteQuestion(questionId)
        );

        assertSame(LmsErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(diagnosisAnswerRepository);
    }
}
