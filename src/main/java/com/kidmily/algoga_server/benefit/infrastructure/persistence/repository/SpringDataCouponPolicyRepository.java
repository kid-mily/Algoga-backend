package com.kidmily.algoga_server.benefit.infrastructure.persistence.repository;

import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.CouponPolicyJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCouponPolicyRepository extends JpaRepository<CouponPolicyJpaEntity, Long> {

    List<CouponPolicyJpaEntity> findByCourseId(Long courseId);

    // 어드민 전체 강의 통합 쿠폰 정책 목록 검색. 각 파라미터가 null 이면 해당 조건을 건너뛴다.
    @Query(value = "SELECT c FROM CouponPolicyJpaEntity c "
            + "WHERE (:courseId IS NULL OR c.courseId = :courseId) "
            + "AND (:active IS NULL OR c.active = :active) "
            + "AND (:keyword IS NULL OR c.couponName LIKE CONCAT('%', :keyword, '%')) "
            + "ORDER BY c.createdAt DESC",
            countQuery = "SELECT COUNT(c) FROM CouponPolicyJpaEntity c "
            + "WHERE (:courseId IS NULL OR c.courseId = :courseId) "
            + "AND (:active IS NULL OR c.active = :active) "
            + "AND (:keyword IS NULL OR c.couponName LIKE CONCAT('%', :keyword, '%'))")
    Page<CouponPolicyJpaEntity> searchForAdmin(
            @Param("courseId") Long courseId,
            @Param("active") Boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<CouponPolicyJpaEntity> findByCourseIdAndActiveTrue(Long courseId);

    Optional<CouponPolicyJpaEntity> findByIdAndCourseIdAndActiveTrue(Long id, Long courseId);

    boolean existsByCourseIdAndActiveTrue(Long courseId);

    boolean existsByCourseIdAndCouponName(Long courseId, String couponName);

    boolean existsByCourseIdAndCouponNameAndIdNot(Long courseId, String couponName, Long id);
}
