package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateDiagnosisQuestionCommand;
import com.kidmily.algoga_server.lms.application.command.SubmitDiagnosisCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateDiagnosisQuestionCommand;
import com.kidmily.algoga_server.lms.application.result.AdminDiagnosisResult;
import com.kidmily.algoga_server.lms.application.result.DiagnosisResultView;
import com.kidmily.algoga_server.lms.application.result.DiagnosisQuestionResult;

import java.util.List;

public interface DiagnosisUseCase {

    List<DiagnosisQuestionResult> getQuestions(Long countryId);

    List<DiagnosisQuestionResult> getAdminQuestions(Long countryId);

    DiagnosisQuestionResult createQuestion(CreateDiagnosisQuestionCommand command);

    DiagnosisQuestionResult updateQuestion(Long questionId, UpdateDiagnosisQuestionCommand command);

    DiagnosisResultView submitResult(SubmitDiagnosisCommand command);

    DiagnosisResultView getLatestResult(Long userId);

    List<AdminDiagnosisResult> getAdminResults(Long userId, Long countryId);

    void deleteQuestion(Long questionId);
}
