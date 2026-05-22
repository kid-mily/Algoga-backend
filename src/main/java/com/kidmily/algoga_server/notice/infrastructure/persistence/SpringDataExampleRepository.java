package com.kidmily.algoga_server.notice.infrastructure.persistence;

import com.kidmily.algoga_server.example.infrastructure.persistence.ExampleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExampleRepository extends JpaRepository<ExampleJpaEntity, Long> {
    // 필요한 경우 Spring Data JPA 쿼리 메서드 추가 (예: findByName)
}