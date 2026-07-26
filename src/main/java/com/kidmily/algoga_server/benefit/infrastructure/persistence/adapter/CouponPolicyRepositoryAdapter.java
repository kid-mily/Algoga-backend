package com.kidmily.algoga_server.benefit.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import com.kidmily.algoga_server.benefit.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.CouponPolicyJpaEntity;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.repository.SpringDataCouponPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponPolicyRepositoryAdapter implements CouponPolicyRepository {

    private final SpringDataCouponPolicyRepository springDataCouponPolicyRepository;

    @Override
    public CouponPolicy save(CouponPolicy couponPolicy) {
        CouponPolicyJpaEntity entity = new CouponPolicyJpaEntity(
                couponPolicy.getCourseId(),
                couponPolicy.getManagerId(),
                couponPolicy.getCouponName(),
                couponPolicy.getDiscountType(),
                couponPolicy.getDiscountValue(),
                couponPolicy.getValidDays(),
                couponPolicy.isActive()
        );

        CouponPolicyJpaEntity savedEntity = springDataCouponPolicyRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<CouponPolicy> findAll() {
        return springDataCouponPolicyRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CouponPolicy> findByCourseId(Long courseId) {
        return springDataCouponPolicyRepository.findByCourseId(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CouponPolicy> findActiveByCourseId(Long courseId) {
        return springDataCouponPolicyRepository.findByCourseIdAndActiveTrue(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Page<CouponPolicy> searchForAdmin(Long courseId, Boolean active, String keyword, Pageable pageable) {
        return springDataCouponPolicyRepository.searchForAdmin(courseId, active, keyword, pageable)
                .map(this::toDomain);
    }

    @Override
    public Optional<CouponPolicy> findById(Long couponPolicyId) {
        return springDataCouponPolicyRepository.findById(couponPolicyId)
                .map(this::toDomain);
    }

    @Override
    public Optional<CouponPolicy> findActiveByIdAndCourseId(Long couponPolicyId, Long courseId) {
        return springDataCouponPolicyRepository.findByIdAndCourseIdAndActiveTrue(couponPolicyId, courseId)
                .map(this::toDomain);
    }

    @Override
    public void deactivate(CouponPolicy couponPolicy) {
        springDataCouponPolicyRepository.findById(couponPolicy.getId())
                .ifPresent(CouponPolicyJpaEntity::deactivate);
    }

    @Override
    public boolean existsActiveByCourseId(Long courseId) {
        return springDataCouponPolicyRepository.existsByCourseIdAndActiveTrue(courseId);
    }

    @Override
    public boolean existsByCourseIdAndCouponName(Long courseId, String couponName) {
        return springDataCouponPolicyRepository.existsByCourseIdAndCouponName(courseId, couponName);
    }

    @Override
    public boolean existsByCourseIdAndCouponNameAndIdNot(
            Long courseId,
            String couponName,
            Long couponPolicyId
    ) {
        return springDataCouponPolicyRepository.existsByCourseIdAndCouponNameAndIdNot(
                courseId,
                couponName,
                couponPolicyId
        );
    }

    @Override
    public Optional<CouponPolicy> updateBasicInfo(
            Long couponPolicyId,
            Long courseId,
            String couponName,
            String discountType,
            int discountValue,
            int validDays
    ) {
        return springDataCouponPolicyRepository.findByIdAndCourseIdAndActiveTrue(couponPolicyId, courseId)
                .map(entity -> {
                    entity.updateBasicInfo(couponName, discountType, discountValue, validDays);
                    return toDomain(entity);
                });
    }

    @Override
    public boolean deactivate(Long couponPolicyId, Long courseId) {
        return springDataCouponPolicyRepository.findByIdAndCourseIdAndActiveTrue(couponPolicyId, courseId)
                .map(entity -> {
                    entity.deactivate();
                    return true;
                })
                .orElse(false);
    }

    private CouponPolicy toDomain(CouponPolicyJpaEntity entity) {
        return CouponPolicy.withId(
                entity.getId(),
                entity.getCourseId(),
                entity.getManagerId(),
                entity.getCouponName(),
                entity.getDiscountType(),
                entity.getDiscountValue(),
                entity.getValidDays(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
