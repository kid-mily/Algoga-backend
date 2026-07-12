package com.kidmily.algoga_server.quiz.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.quiz.domain.model.QuizSubmissionAnswer;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionAnswerRepository;
import com.kidmily.algoga_server.quiz.infrastructure.persistence.entity.QuizSubmissionAnswerJpaEntity;
import com.kidmily.algoga_server.quiz.infrastructure.persistence.repository.SpringDataQuizSubmissionAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuizSubmissionAnswerRepositoryAdapter implements QuizSubmissionAnswerRepository {

    private final SpringDataQuizSubmissionAnswerRepository springDataQuizSubmissionAnswerRepository;

    @Override
    public List<QuizSubmissionAnswer> saveAll(List<QuizSubmissionAnswer> answers) {
        List<QuizSubmissionAnswerJpaEntity> entities = answers.stream()
                .map(this::toEntity)
                .toList();

        return springDataQuizSubmissionAnswerRepository.saveAll(entities)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<QuizSubmissionAnswer> findBySubmissionId(Long submissionId) {
        return springDataQuizSubmissionAnswerRepository.findBySubmissionIdOrderByIdAsc(submissionId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteBySubmissionId(Long submissionId) {
        springDataQuizSubmissionAnswerRepository.deleteBySubmissionId(submissionId);
    }

    private QuizSubmissionAnswerJpaEntity toEntity(QuizSubmissionAnswer answer) {
        return new QuizSubmissionAnswerJpaEntity(
                answer.getSubmissionId(),
                answer.getQuizId(),
                answer.getQuestion(),
                answer.getOption1(),
                answer.getOption2(),
                answer.getOption3(),
                answer.getOption4(),
                answer.getSelectedOption(),
                answer.getCorrectOption(),
                answer.isCorrect(),
                answer.getExplanation()
        );
    }

    private QuizSubmissionAnswer toDomain(QuizSubmissionAnswerJpaEntity entity) {
        return QuizSubmissionAnswer.withId(
                entity.getId(),
                entity.getSubmissionId(),
                entity.getQuizId(),
                entity.getQuestion(),
                entity.getOption1(),
                entity.getOption2(),
                entity.getOption3(),
                entity.getOption4(),
                entity.getSelectedOption(),
                entity.getCorrectOption(),
                entity.isCorrect(),
                entity.getExplanation()
        );
    }
}