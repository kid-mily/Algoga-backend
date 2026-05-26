package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.UserCoupon;
import com.kidmily.algoga_server.lms.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.UserCouponJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataUserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryAdapter implements UserCouponRepository {

    private final SpringDataUserCouponRepository springDataUserCouponRepository;

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        UserCouponJpaEntity entity = new UserCouponJpaEntity(
                userCoupon.getUserId(),
                userCoupon.getCourseId(),
                userCoupon.getCouponPolicyId(),
                userCoupon.getCouponName(),
                userCoupon.getDiscountType(),
                userCoupon.getDiscountValue(),
                userCoupon.getStatus(),
                userCoupon.getIssuedAt(),
                userCoupon.getExpiredAt(),
                userCoupon.getUsedAt()
        );

        UserCouponJpaEntity savedEntity = springDataUserCouponRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<UserCoupon> findByUserId(Long userId) {
        return springDataUserCouponRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<UserCoupon> findByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataUserCouponRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndCouponPolicyId(
            Long userId,
            Long couponPolicyId
    ) {
        return springDataUserCouponRepository.existsByUserIdAndCouponPolicyId(userId, couponPolicyId);
    }

    private UserCoupon toDomain(UserCouponJpaEntity entity) {
        return UserCoupon.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getCouponPolicyId(),
                entity.getCouponName(),
                entity.getDiscountType(),
                entity.getDiscountValue(),
                entity.getStatus(),
                entity.getIssuedAt(),
                entity.getExpiredAt(),
                entity.getUsedAt()
        );
    }
}