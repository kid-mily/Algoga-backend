package com.kidmily.algoga_server.benefit.presentation.api.admin;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.benefit.application.command.CreateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.command.UpdateCouponPolicyCommand;
import com.kidmily.algoga_server.benefit.application.usecase.CouponPolicyUseCase;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.presentation.request.CreateCouponPolicyRequest;
import com.kidmily.algoga_server.benefit.presentation.request.UpdateCouponPolicyRequest;
import com.kidmily.algoga_server.benefit.presentation.response.CouponPolicyResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin Coupon", description = "강의별 쿠폰 정책 관리 API")
@RestController
@RequestMapping("/api/v1/admin/courses/{courseId}/coupon-policies")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponPolicyUseCase couponPolicyUseCase;

    @Operation(
            summary = "강의별 쿠폰 정책 등록",
            description = "강의 수료 보상으로 지급할 활성 쿠폰 정책을 등록합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "INVALID_COUPON_POLICY",
            "DUPLICATED_COUPON_POLICY_NAME"
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
                request.discountValue()
        );

        var couponPolicy = couponPolicyUseCase.createCouponPolicy(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COUPON_POLICY_CREATED",
                        "쿠폰 정책 등록에 성공했습니다.",
                        CouponPolicyResponse.from(couponPolicy)
                ));
    }

    @Operation(
            summary = "강의별 쿠폰 정책 목록 조회",
            description = "특정 강의에 등록된 활성 쿠폰 정책 목록을 조회합니다."
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
                        "쿠폰 정책 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "강의별 쿠폰 정책 수정",
            description = "특정 강의에 등록된 활성 쿠폰 정책을 수정합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "COUPON_POLICY_NOT_FOUND",
            "INVALID_COUPON_POLICY",
            "DUPLICATED_COUPON_POLICY_NAME"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{couponPolicyId}")
    public ResponseEntity<ApiResponse<CouponPolicyResponse>> updateCouponPolicy(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "쿠폰 정책 ID", example = "12")
            @PathVariable Long couponPolicyId,

            @Valid @RequestBody UpdateCouponPolicyRequest request
    ) {
        UpdateCouponPolicyCommand command = new UpdateCouponPolicyCommand(
                courseId,
                couponPolicyId,
                request.couponName(),
                request.discountType(),
                request.discountValue()
        );

        var couponPolicy = couponPolicyUseCase.updateCouponPolicy(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COUPON_POLICY_UPDATED",
                        "쿠폰 정책 수정에 성공했습니다.",
                        CouponPolicyResponse.from(couponPolicy)
                )
        );
    }

    @Operation(
            summary = "강의별 쿠폰 정책 삭제",
            description = "쿠폰 정책을 실제 삭제하지 않고 비활성화합니다."
    )
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {"COURSE_NOT_FOUND", "COUPON_POLICY_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{couponPolicyId}")
    public ResponseEntity<ApiResponse<Void>> deleteCouponPolicy(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "쿠폰 정책 ID", example = "12")
            @PathVariable Long couponPolicyId
    ) {
        couponPolicyUseCase.deactivateCouponPolicy(courseId, couponPolicyId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COUPON_POLICY_DELETED",
                        "쿠폰 정책 삭제에 성공했습니다.",
                        null
                )
        );
    }
}
