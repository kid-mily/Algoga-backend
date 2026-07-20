package com.kidmily.algoga_server.qna.domain.repository;

import com.kidmily.algoga_server.qna.domain.model.CourseQna;

import java.util.List;
import java.util.Optional;

public interface CourseQnaRepository {

    CourseQna save(CourseQna courseQna);

    Optional<CourseQna> findById(Long qnaId);

    Optional<CourseQna> findByIdAndCourseId(Long qnaId, Long courseId);

    List<CourseQna> findByCourseId(Long courseId);

    List<Long> findIdsByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}