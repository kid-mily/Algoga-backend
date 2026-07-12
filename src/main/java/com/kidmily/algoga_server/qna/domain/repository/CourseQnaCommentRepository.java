package com.kidmily.algoga_server.qna.domain.repository;

import com.kidmily.algoga_server.qna.domain.model.CourseQnaComment;

import java.util.List;
import java.util.Optional;

public interface CourseQnaCommentRepository {

    CourseQnaComment save(CourseQnaComment comment);

    Optional<CourseQnaComment> findByIdAndQnaId(Long commentId, Long qnaId);

    List<CourseQnaComment> findByQnaId(Long qnaId);
}