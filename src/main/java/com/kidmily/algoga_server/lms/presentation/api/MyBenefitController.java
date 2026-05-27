package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.usecase.MyBenefitUseCase;
import com.kidmily.algoga_server.lms.presentation.response.MyCouponResponse;
import com.kidmily.algoga_server.lms.presentation.response.MyMileageResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "마이페이지 혜택", description = "마이페이지 쿠폰함, 마일리지 내역 조회 API")
@RestController
@RequestMapping("/api/v1/my")
@RequiredArgsConstructor
public class MyBenefitController {

    private final MyBenefitUseCase myBenefitUseCase;

    @Operation(
            summary = "내 쿠폰함 조회",
            description = "로그인한 사용자가 보유한 쿠폰 목록을 조회합니다."
    )
    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<MyCouponResponse>>> getMyCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        List<MyCouponResponse> response = myBenefitUseCase.getMyCoupons(currentUserId)
                .stream()
                .map(MyCouponResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MY_COUPONS_FOUND",
                        "내 쿠폰함 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "내 마일리지 내역 조회",
            description = "로그인한 사용자의 현재 보유 마일리지와 마일리지 적립/사용 내역을 조회합니다."
    )
    @GetMapping("/mileages")
    public ResponseEntity<ApiResponse<MyMileageResponse>> getMyMileages(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        MyMileageResponse response = MyMileageResponse.from(
                myBenefitUseCase.getMyMileages(currentUserId)
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MY_MILEAGES_FOUND",
                        "내 마일리지 내역 조회에 성공했습니다.",
                        response
                )
        );
    }
}