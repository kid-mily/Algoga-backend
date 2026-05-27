package com.kidmily.algoga_server.benefit.presentation.api.admin;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.benefit.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.usecase.CouponPolicyUseCase;
import com.kidmily.algoga_server.benefit.domain.model.CouponPolicy;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.presentation.request.CreateCouponPolicyRequest;
import com.kidmily.algoga_server.benefit.presentation.response.CouponPolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Coupon", description = "콘텐츠 매니저 강의별 쿠폰 정책 관리 API")
@RestController
@RequestMapping("/api/v1/admin/courses/{courseId}/coupon-policies")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponPolicyUseCase couponPolicyUseCase;

    @Operation(
            summary = "강의별 쿠폰 정책 등록",
            description = """
                    콘텐츠 매니저가 특정 강의에 수료 보상으로 지급할 쿠폰 정책을 등록합니다.
                    할인 타입은 RATE 또는 AMOUNT를 사용할 수 있습니다.
                    """
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "INVALID_COUPON_POLICY"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CouponPolicyResponse>> createCouponPolicy(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Valid @RequestBody CreateCouponPolicyRequest request,

            @CurrentManager Long managerId
    ) {
        CreateCouponPolicyCommand command = new CreateCouponPolicyCommand(
                courseId,
                managerId,
                request.couponName(),
                request.discountType(),
                request.discountValue(),
                request.validDays()
        );

        CouponPolicy couponPolicy = couponPolicyUseCase.createCouponPolicy(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COUPON_POLICY_CREATED",
                        "강의별 쿠폰 정책 등록에 성공했습니다.",
                        CouponPolicyResponse.from(couponPolicy)
                ));
    }

    @Operation(
            summary = "강의별 쿠폰 정책 목록 조회",
            description = "특정 강의에 등록된 쿠폰 정책 목록을 조회합니다."
    )
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponPolicyResponse>>> getCouponPolicies(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId
    ) {
        List<CouponPolicyResponse> response = couponPolicyUseCase.getCouponPolicies(courseId)
                .stream()
                .map(CouponPolicyResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COUPON_POLICIES_FOUND",
                        "강의별 쿠폰 정책 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}