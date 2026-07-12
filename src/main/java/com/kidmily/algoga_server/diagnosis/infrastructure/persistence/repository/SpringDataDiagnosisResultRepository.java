package com.kidmily.algoga_server.diagnosis.infrastructure.persistence.repository;

import com.kidmily.algoga_server.diagnosis.infrastructure.persistence.entity.DiagnosisResultJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataDiagnosisResultRepository extends JpaRepository<DiagnosisResultJpaEntity, Long> {
    @Query("""
            SELECT d
            FROM DiagnosisResultJpaEntity d
            WHERE d.id IN (
                SELECT MAX(latest.id)
                FROM DiagnosisResultJpaEntity latest
                WHERE latest.userId = :userId
                GROUP BY latest.countryId
            )
            ORDER BY d.createdAt DESC
            """)
    List<DiagnosisResultJpaEntity> findLatestByUserIdGroupByCountry(@Param("userId") Long userId);


    List<DiagnosisResultJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<DiagnosisResultJpaEntity> findByCountryIdOrderByCreatedAtDesc(Long countryId);

    List<DiagnosisResultJpaEntity> findByUserIdAndCountryIdOrderByCreatedAtDesc(Long userId, Long countryId);

    List<DiagnosisResultJpaEntity> findAllByOrderByCreatedAtDesc();
}