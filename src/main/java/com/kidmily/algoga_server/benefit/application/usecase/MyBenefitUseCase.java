package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.result.MyCouponResult;
import com.kidmily.algoga_server.benefit.application.result.MyMileageResult;

import java.util.List;

public interface MyBenefitUseCase {

    List<MyCouponResult> getMyCoupons(Long userId);

    MyMileageResult getMyMileages(Long userId);
}