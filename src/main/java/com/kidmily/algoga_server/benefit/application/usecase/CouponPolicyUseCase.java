package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.CreateCouponPolicyCommand;
<<<<<<< HEAD
import com.kidmily.algoga_server.benefit.application.result.CouponPolicyResult;
=======
import com.kidmily.algoga_server.benefit.application.command.UpdateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
>>>>>>> 9e394e2220795389f2b87882ee1f5f7586ebffc6

import java.util.List;

public interface CouponPolicyUseCase {

    CouponPolicyResult createCouponPolicy(CreateCouponPolicyCommand command);

<<<<<<< HEAD
    List<CouponPolicyResult> getCouponPolicies(Long courseId);

    void deleteCouponPolicy(Long courseId, Long couponPolicyId);
=======
    List<CouponPolicy> getCouponPolicies(Long courseId);

    CouponPolicy updateCouponPolicy(UpdateCouponPolicyCommand command);

    void deactivateCouponPolicy(Long courseId, Long couponPolicyId);
>>>>>>> 9e394e2220795389f2b87882ee1f5f7586ebffc6
}
