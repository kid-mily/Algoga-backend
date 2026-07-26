package com.kidmily.algoga_server.diagnosis.application.usecase;

import com.kidmily.algoga_server.diagnosis.application.command.CreateDiagnosisQuestionCommand;
import com.kidmily.algoga_server.diagnosis.application.command.SubmitDiagnosisCommand;
import com.kidmily.algoga_server.diagnosis.application.command.UpdateDiagnosisQuestionCommand;
import com.kidmily.algoga_server.diagnosis.application.result.AdminDiagnosisResult;
import com.kidmily.algoga_server.diagnosis.application.result.DiagnosisQuestionResult;
import com.kidmily.algoga_server.diagnosis.application.result.DiagnosisResultView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiagnosisUseCase {

    List<DiagnosisQuestionResult> getQuestions(Long countryId);

    List<DiagnosisQuestionResult> getAdminQuestions(Long countryId);

    DiagnosisQuestionResult createQuestion(CreateDiagnosisQuestionCommand command);

    DiagnosisQuestionResult updateQuestion(Long questionId, UpdateDiagnosisQuestionCommand command);

    DiagnosisResultView submitResult(SubmitDiagnosisCommand command);

    List<DiagnosisResultView> getLatestResultsByCountry(Long userId);


    Page<AdminDiagnosisResult> getAdminResults(Long userId, Long countryId, Pageable pageable);

    void deleteQuestion(Long questionId);
}