package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.QuizSubmissionAnswerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataQuizSubmissionAnswerRepository extends JpaRepository<QuizSubmissionAnswerJpaEntity, Long> {

    List<QuizSubmissionAnswerJpaEntity> findBySubmissionIdOrderByIdAsc(Long submissionId);

    void deleteBySubmissionId(Long submissionId);
}