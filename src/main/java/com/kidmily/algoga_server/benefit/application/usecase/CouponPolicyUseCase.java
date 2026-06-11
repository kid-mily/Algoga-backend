package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.result.CouponPolicyResult;

import java.util.List;

public interface CouponPolicyUseCase {

    CouponPolicyResult createCouponPolicy(CreateCouponPolicyCommand command);

    List<CouponPolicyResult> getCouponPolicies(Long courseId);

    void deleteCouponPolicy(Long courseId, Long couponPolicyId);
}
