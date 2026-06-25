package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.QuizSubmissionAnswer;

import java.util.List;

public interface QuizSubmissionAnswerRepository {

    List<QuizSubmissionAnswer> saveAll(List<QuizSubmissionAnswer> answers);

    List<QuizSubmissionAnswer> findBySubmissionId(Long submissionId);

    void deleteBySubmissionId(Long submissionId);
}