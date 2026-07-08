package com.kidmily.algoga_server.course.infrastructure.persistence.repository;

import com.kidmily.algoga_server.course.infrastructure.persistence.entity.CourseJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, Long> {

    Optional<CourseJpaEntity> findByIdAndDeletedFalse(Long id);

    List<CourseJpaEntity> findByIdIn(List<Long> ids);

    Page<CourseJpaEntity> findByDeletedFalseOrderByIdDesc(Pageable pageable);

    Page<CourseJpaEntity> findByDeletedFalseAndCountryIdInOrderByIdDesc(
            List<Long> countryIds,
            Pageable pageable
    );

    Page<CourseJpaEntity> findByDeletedTrueOrderByIdDesc(Pageable pageable);

    Page<CourseJpaEntity> findByDeletedTrueAndCountryIdInOrderByIdDesc(
            List<Long> countryIds,
            Pageable pageable
    );

    List<CourseJpaEntity> findByCountryIdAndStatusAndDeletedFalseOrderByIdDesc(
            Long countryId,
            String status
    );

    List<CourseJpaEntity> findByCountryIdAndLevelAndStatusAndDeletedFalseOrderByIdDesc(
            Long countryId,
            String level,
            String status
    );

    long countByCountryIdAndStatusAndDeletedFalse(
            Long countryId,
            String status
    );

    @Query("""
            SELECT c.countryId, COUNT(c.id)
            FROM CourseJpaEntity c
            WHERE c.countryId IN :countryIds
              AND c.status = :status
              AND c.deleted = false
            GROUP BY c.countryId
            """)
    List<Object[]> countByCountryIdsAndStatus(
            @Param("countryIds") List<Long> countryIds,
            @Param("status") String status
    );
}

