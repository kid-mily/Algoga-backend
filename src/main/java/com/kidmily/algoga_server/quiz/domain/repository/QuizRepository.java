package com.kidmily.algoga_server.quiz.domain.repository;

import com.kidmily.algoga_server.quiz.domain.model.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface QuizRepository {

    Quiz save(Quiz quiz);

    List<Quiz> findByCourseId(Long courseId);

    Page<Quiz> searchForAdmin(Long courseId, String keyword, Pageable pageable);

    long countByCourseId(Long courseId);

    Optional<Quiz> findByIdAndCourseId(Long quizId, Long courseId);

    Optional<Quiz> updateBasicInfo(
            Long quizId,
            Long courseId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation
    );

    boolean delete(Long quizId, Long courseId);
}
