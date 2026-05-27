package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.result.MyCouponResult;
import com.kidmily.algoga_server.lms.application.result.MyMileageResult;

import java.util.List;

public interface MyBenefitUseCase {

    List<MyCouponResult> getMyCoupons(Long userId);

    MyMileageResult getMyMileages(Long userId);
}