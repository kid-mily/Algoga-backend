package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, Long> {

    List<CourseJpaEntity> findByCountryIdAndStatusOrderByIdDesc(Long countryId, String status);

    long countByCountryIdAndStatus(Long countryId, String status);

    @Query("""
            SELECT c.countryId, COUNT(c.id)
            FROM CourseJpaEntity c
            WHERE c.countryId IN :countryIds
              AND c.status = :status
            GROUP BY c.countryId
            """)
    List<Object[]> countByCountryIdsAndStatus(
            @Param("countryIds") List<Long> countryIds,
            @Param("status") String status
    );
}