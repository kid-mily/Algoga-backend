package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.SubmitDiagnosisCommand;
import com.kidmily.algoga_server.lms.application.result.DiagnosisResultView;
import com.kidmily.algoga_server.lms.application.result.DiagnosisQuestionResult;

import java.util.List;

public interface DiagnosisUseCase {

    List<DiagnosisQuestionResult> getQuestions(Long countryId);

    DiagnosisResultView submitResult(SubmitDiagnosisCommand command);

    DiagnosisResultView getLatestResult(Long userId);
}
