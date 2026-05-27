package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.MileageHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataMileageHistoryRepository extends JpaRepository<MileageHistoryJpaEntity, Long> {

    List<MileageHistoryJpaEntity> findByUserId(Long userId);

    List<MileageHistoryJpaEntity> findByUserIdAndCourseId(Long userId, Long courseId);
}