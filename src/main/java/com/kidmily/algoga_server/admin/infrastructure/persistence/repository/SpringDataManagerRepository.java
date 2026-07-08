package com.kidmily.algoga_server.admin.infrastructure.persistence.repository;

import com.kidmily.algoga_server.admin.infrastructure.persistence.entity.ManagerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SpringDataManagerRepository extends JpaRepository<ManagerJpaEntity, Long> {
    Optional<ManagerJpaEntity> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);

    // super_admin(pk=1) 제외 전체 조회
    @Query("SELECT m FROM ManagerJpaEntity m WHERE m.id <> :excludeId")
    List<ManagerJpaEntity> findAllExcludingId(@Param("excludeId") Long excludeId);

    // super_admin(pk=1) 제외 아이디 또는 이름으로 LIKE 검색
    @Query("SELECT m FROM ManagerJpaEntity m WHERE m.id <> :excludeId " +
            "AND (m.loginId LIKE %:keyword% OR m.name LIKE %:keyword%)")
    List<ManagerJpaEntity> searchByKeywordExcludingId(@Param("keyword") String keyword,
                                                      @Param("excludeId") Long excludeId);
}