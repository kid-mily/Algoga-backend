package com.kidmily.algoga_server.benefit.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.benefit.domain.model.CourseReward;
import com.kidmily.algoga_server.benefit.domain.repository.CourseRewardRepository;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.entity.CourseRewardJpaEntity;
import com.kidmily.algoga_server.benefit.infrastructure.persistence.repository.SpringDataCourseRewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseRewardRepositoryAdapter implements CourseRewardRepository {

    private final SpringDataCourseRewardRepository springDataCourseRewardRepository;

    @Override
    public CourseReward save(CourseReward courseReward) {
        CourseRewardJpaEntity entity = new CourseRewardJpaEntity(
                courseReward.getUserId(),
                courseReward.getCourseId(),
                courseReward.getIssuedCouponCount(),
                courseReward.getMileageHistoryId(),
                courseReward.getRewardedAt()
        );

        CourseRewardJpaEntity savedEntity = springDataCourseRewardRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<CourseReward> findByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataCourseRewardRepository.findByUserIdAndCourseId(userId, courseId)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByUserIdAndCourseId(
            Long userId,
            Long courseId
    ) {
        return springDataCourseRewardRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    private CourseReward toDomain(CourseRewardJpaEntity entity) {
        return CourseReward.withId(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                entity.getIssuedCouponCount(),
                entity.getMileageHistoryId(),
                entity.getRewardedAt()
        );
    }
}