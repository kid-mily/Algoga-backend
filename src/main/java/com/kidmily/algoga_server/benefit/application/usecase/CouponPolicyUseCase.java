package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.command.UpdateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;

import java.util.List;

public interface CouponPolicyUseCase {

    CouponPolicy createCouponPolicy(CreateCouponPolicyCommand command);

    List<CouponPolicy> getCouponPolicies(Long courseId);

    CouponPolicy updateCouponPolicy(UpdateCouponPolicyCommand command);

    void deactivateCouponPolicy(Long courseId, Long couponPolicyId);
}
