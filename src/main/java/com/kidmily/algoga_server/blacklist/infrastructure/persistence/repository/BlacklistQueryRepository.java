package com.kidmily.algoga_server.blacklist.infrastructure.persistence.repository;

import com.kidmily.algoga_server.blacklist.infrastructure.persistence.entity.BlacklistJpaEntity;
import com.kidmily.algoga_server.blacklist.infrastructure.persistence.query.BlacklistUserQueryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlacklistQueryRepository extends JpaRepository<BlacklistJpaEntity, Long> {

    @Query(value = """
            SELECT 
                u.user_id AS userId, u.username AS username, u.name AS name, 
                u.nickname AS nickname, u.email AS email,
                (SELECT COUNT(r.report_id) FROM reports r WHERE r.reported_user_id = u.user_id AND r.status = 'COMPLETED') AS reportCount,
                (SELECT MAX(r.created_at) FROM reports r WHERE r.reported_user_id = u.user_id AND r.status = 'COMPLETED') AS lastReportedAt,
                (SELECT COUNT(b.id) FROM blacklists b WHERE b.user_id = u.user_id AND b.status = 'ACTIVE') AS isBlacklisted
            FROM users u
            WHERE u.is_deleted = false
              AND (SELECT COUNT(r.report_id) FROM reports r WHERE r.reported_user_id = u.user_id AND r.status = 'COMPLETED') >= 5
            """,
            countQuery = """
            SELECT COUNT(u.user_id) 
            FROM users u 
            WHERE u.is_deleted = false 
              AND (SELECT COUNT(r.report_id) FROM reports r WHERE r.reported_user_id = u.user_id AND r.status = 'COMPLETED') >= 5
            """,
            nativeQuery = true)
    Page<BlacklistUserQueryProjection> findAllCandidateUsers(Pageable pageable);

    @Query(value = """
            SELECT 
                u.user_id AS userId, u.username AS username, u.name AS name, 
                u.nickname AS nickname, u.email AS email,
                (SELECT COUNT(r.report_id) FROM reports r WHERE r.reported_user_id = u.user_id AND r.status = 'COMPLETED') AS reportCount,
                (SELECT MAX(r.created_at) FROM reports r WHERE r.reported_user_id = u.user_id AND r.status = 'COMPLETED') AS lastReportedAt,
                (SELECT COUNT(b.id) FROM blacklists b WHERE b.user_id = u.user_id AND b.status = 'ACTIVE') AS isBlacklisted
            FROM users u
            WHERE u.user_id = :userId AND u.is_deleted = false
            """, nativeQuery = true)
    Optional<BlacklistUserQueryProjection> findCandidateUserDetailById(@Param("userId") Long userId);
}