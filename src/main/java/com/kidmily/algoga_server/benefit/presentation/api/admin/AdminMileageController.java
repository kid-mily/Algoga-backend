package com.kidmily.algoga_server.benefit.presentation.api.admin;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.benefit.application.command.AdminMileageTransactionCommand;
import com.kidmily.algoga_server.benefit.application.result.AdminMileageHistoryResult;
import com.kidmily.algoga_server.benefit.application.usecase.MileageUseCase;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.presentation.request.AdminMileageTransactionRequest;
import com.kidmily.algoga_server.benefit.presentation.response.AdminMileageHistoryResponse;
import com.kidmily.algoga_server.benefit.presentation.response.AdminMileageSummaryResponse;
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

@Tag(name = "Admin Mileage", description = "콘텐츠 매니저 마일리지 관리 API")
@RestController
@RequestMapping("/api/v1/admin/mileages")
@RequiredArgsConstructor
public class AdminMileageController {

    private final MileageUseCase mileageUseCase;

    @Operation(
            summary = "사용자별 마일리지 목록 조회",
            description = "마일리지 내역이 있는 사용자들의 현재 보유 마일리지, 총 적립, 총 사용 금액을 조회합니다."
    )
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminMileageSummaryResponse>> getMileageUsers() {
        AdminMileageSummaryResponse response = AdminMileageSummaryResponse.from(
                mileageUseCase.getMileageUsers()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_MILEAGES_FOUND",
                        "관리자 마일리지 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "사용자 마일리지 상세 내역 조회",
            description = "특정 사용자의 마일리지 적립/사용/회수 내역을 조회합니다."
    )
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {"MILEAGE_USER_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping("/users/{userId}/histories")
    public ResponseEntity<ApiResponse<List<AdminMileageHistoryResponse>>> getUserMileageHistories(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId
    ) {
        List<AdminMileageHistoryResponse> response = mileageUseCase.getUserMileageHistories(userId)
                .stream()
                .map(AdminMileageHistoryResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_MILEAGE_HISTORIES_FOUND",
                        "사용자 마일리지 상세 내역 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "마일리지 지급",
            description = "콘텐츠 매니저가 특정 사용자에게 마일리지를 지급합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
            "MILEAGE_USER_NOT_FOUND",
            "INVALID_MILEAGE_AMOUNT"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/users/{userId}/earn")
    public ResponseEntity<ApiResponse<AdminMileageHistoryResponse>> earnMileage(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,

            @Valid @RequestBody AdminMileageTransactionRequest request,

            @CurrentManager Long managerId
    ) {
        AdminMileageTransactionCommand command = new AdminMileageTransactionCommand(
                userId,
                managerId,
                request.amount(),
                request.reason()
        );

        AdminMileageHistoryResult result = mileageUseCase.earnMileage(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "ADMIN_MILEAGE_EARNED",
                        "마일리지 지급에 성공했습니다.",
                        AdminMileageHistoryResponse.from(result)
                ));
    }

    @Operation(
            summary = "마일리지 회수",
            description = "콘텐츠 매니저가 특정 사용자의 마일리지를 회수합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
            "MILEAGE_USER_NOT_FOUND",
            "INVALID_MILEAGE_AMOUNT",
            "NOT_ENOUGH_MILEAGE"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/users/{userId}/use")
    public ResponseEntity<ApiResponse<AdminMileageHistoryResponse>> useMileage(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,

            @Valid @RequestBody AdminMileageTransactionRequest request,

            @CurrentManager Long managerId
    ) {
        AdminMileageTransactionCommand command = new AdminMileageTransactionCommand(
                userId,
                managerId,
                request.amount(),
                request.reason()
        );

        AdminMileageHistoryResult result = mileageUseCase.useMileage(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "ADMIN_MILEAGE_USED",
                        "마일리지 회수에 성공했습니다.",
                        AdminMileageHistoryResponse.from(result)
                ));
    }
}