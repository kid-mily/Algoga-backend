package com.kidmily.algoga_server.benefit.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import com.kidmily.algoga_server.benefit.domain.repository.CouponPolicyRepository;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.CouponPolicyJpaEntity;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.repository.SpringDataCouponPolicyRepository;
import lombok.RequiredArgsConstructor;
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
    public Optional<CouponPolicy> findById(Long couponPolicyId) {
        return springDataCouponPolicyRepository.findById(couponPolicyId)
                .map(this::toDomain);
    }

    @Override
    public boolean existsActiveByCourseId(Long courseId) {
        return springDataCouponPolicyRepository.existsByCourseIdAndActiveTrue(courseId);
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