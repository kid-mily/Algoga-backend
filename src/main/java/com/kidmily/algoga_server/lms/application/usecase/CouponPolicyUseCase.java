package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.lms.domain.model.CouponPolicy;

import java.util.List;

public interface CouponPolicyUseCase {

    CouponPolicy createCouponPolicy(CreateCouponPolicyCommand command);

    List<CouponPolicy> getCouponPolicies(Long courseId);
}