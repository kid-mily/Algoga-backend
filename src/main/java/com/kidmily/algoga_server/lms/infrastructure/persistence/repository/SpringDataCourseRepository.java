package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, Long> {
}