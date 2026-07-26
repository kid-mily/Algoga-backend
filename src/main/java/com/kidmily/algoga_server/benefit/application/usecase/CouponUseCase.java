package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.command.IssueWelcomeCouponCommand;
import com.kidmily.algoga_server.benefit.application.command.UpdateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.result.AdminCouponPolicyListItemResult;
import com.kidmily.algoga_server.benefit.application.result.CouponPolicyResult;
import com.kidmily.algoga_server.benefit.application.result.CouponStatisticsResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponUseCase {

    CouponPolicyResult createCouponPolicy(CreateCouponPolicyCommand command);

    List<CouponPolicyResult> getCouponPolicies(Long courseId);

    Page<AdminCouponPolicyListItemResult> getAdminCouponPolicies(Long courseId, Boolean active, String keyword, Pageable pageable);

    CouponPolicyResult updateCouponPolicy(UpdateCouponPolicyCommand command);

    void deactivateCouponPolicy(Long courseId, Long couponPolicyId);

    CouponStatisticsResult getCouponStatistics(Long courseId, Long countryId);

    void issueWelcomeCoupon(IssueWelcomeCouponCommand command);
}
