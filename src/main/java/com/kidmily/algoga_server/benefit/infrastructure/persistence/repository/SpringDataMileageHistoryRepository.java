package com.kidmily.algoga_server.benefit.infrastructure.persistence.repository;

import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.MileageHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataMileageHistoryRepository extends JpaRepository<MileageHistoryJpaEntity, Long> {

    List<MileageHistoryJpaEntity> findAllByOrderByCreatedAtDesc();

    List<MileageHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}