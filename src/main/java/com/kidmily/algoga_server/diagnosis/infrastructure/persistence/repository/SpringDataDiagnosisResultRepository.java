package com.kidmily.algoga_server.diagnosis.infrastructure.persistence.repository;

import com.kidmily.algoga_server.diagnosis.infrastructure.persistence.entity.DiagnosisResultJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    Page<DiagnosisResultJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<DiagnosisResultJpaEntity> findByCountryIdOrderByCreatedAtDesc(Long countryId, Pageable pageable);

    Page<DiagnosisResultJpaEntity> findByUserIdAndCountryIdOrderByCreatedAtDesc(Long userId, Long countryId, Pageable pageable);

    Page<DiagnosisResultJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT d.id FROM DiagnosisResultJpaEntity d WHERE d.userId = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}