package com.kidmily.algoga_server.admin.infrastructure.persistence.repository;

import com.kidmily.algoga_server.admin.infrastructure.persistence.entity.ManagerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataManagerRepository extends JpaRepository<ManagerJpaEntity, Long> {
    Optional<ManagerJpaEntity> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);

    // 🌟 아이디 또는 이름으로 LIKE 검색
    List<ManagerJpaEntity> findByLoginIdContainingOrNameContaining(String loginId, String name);
}