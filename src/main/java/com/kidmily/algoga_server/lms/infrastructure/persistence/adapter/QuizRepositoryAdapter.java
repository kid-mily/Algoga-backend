package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.Quiz;
import com.kidmily.algoga_server.lms.domain.repository.QuizRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.QuizJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuizRepositoryAdapter implements QuizRepository {

    private final SpringDataQuizRepository springDataQuizRepository;

    @Override
    public Quiz save(Quiz quiz) {
        QuizJpaEntity entity = new QuizJpaEntity(
                quiz.getCourseId(),
                quiz.getQuestion(),
                quiz.getOption1(),
                quiz.getOption2(),
                quiz.getOption3(),
                quiz.getOption4(),
                quiz.getCorrectOption(),
                quiz.getExplanation()
        );

        QuizJpaEntity savedEntity = springDataQuizRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<Quiz> findByCourseId(Long courseId) {
        return springDataQuizRepository.findByCourseIdAndDeletedFalseOrderByIdAsc(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Quiz> findByIdAndCourseId(Long quizId, Long courseId) {
        return springDataQuizRepository.findByIdAndCourseIdAndDeletedFalse(quizId, courseId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Quiz> updateBasicInfo(
            Long quizId,
            Long courseId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation
    ) {
        return springDataQuizRepository.findByIdAndCourseIdAndDeletedFalse(quizId, courseId)
                .map(entity -> {
                    entity.updateBasicInfo(
                            question,
                            option1,
                            option2,
                            option3,
                            option4,
                            correctOption,
                            explanation
                    );

                    return toDomain(entity);
                });
    }

    @Override
    public boolean softDelete(Long quizId, Long courseId) {
        return springDataQuizRepository.findByIdAndCourseIdAndDeletedFalse(quizId, courseId)
                .map(entity -> {
                    entity.softDelete();
                    return true;
                })
                .orElse(false);
    }

    private Quiz toDomain(QuizJpaEntity entity) {
        return Quiz.withId(
                entity.getId(),
                entity.getCourseId(),
                entity.getQuestion(),
                entity.getOption1(),
                entity.getOption2(),
                entity.getOption3(),
                entity.getOption4(),
                entity.getCorrectOption(),
                entity.getExplanation(),
                entity.isDeleted()
        );
    }
}