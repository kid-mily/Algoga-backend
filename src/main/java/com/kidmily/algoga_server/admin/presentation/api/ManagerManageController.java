package com.kidmily.algoga_server.admin.presentation.api;

import com.kidmily.algoga_server.admin.application.command.CreateManagerCommand;
import com.kidmily.algoga_server.admin.application.command.UpdateManagerCommand;
import com.kidmily.algoga_server.admin.application.usecase.ManagerManageUseCase;
import com.kidmily.algoga_server.admin.exception.ManagerErrorCode;
import com.kidmily.algoga_server.admin.presentation.api.request.ManagerCreateRequest;
import com.kidmily.algoga_server.admin.presentation.api.request.ManagerUpdateRequest;
import com.kidmily.algoga_server.admin.presentation.api.response.ManagerResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Admin Manage", description = "관리자 계정 관리 API (SUPER_ADMIN 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/managers")
@PreAuthorize("hasRole('SUPER_ADMIN')") // 🌟 핵심: 이 컨트롤러의 모든 API는 최고 관리자(SUPER_ADMIN)만 호출 가능합니다.
public class ManagerManageController {

    private final ManagerManageUseCase managerManageUseCase;

    @Operation(summary = "매니저 계정 생성")
    @ApiErrorCodeExample(domain = ManagerErrorCode.class, value = {"ALREADY_EXISTS_LOGIN_ID"})
    @PostMapping
    public ApiResponse<Void> createManager(@RequestBody @Valid ManagerCreateRequest request) {
        log.info("[Manager Create] 계정 생성 요청 - ID: {}, Role: {}", request.loginId(), request.role());

        CreateManagerCommand command = new CreateManagerCommand(
                request.loginId(), request.password(), request.name(),
                request.phone(), request.email(), request.role()
        );
        managerManageUseCase.createManager(command);

        return ApiResponse.created("MANAGER_CREATED", "관리자 계정이 생성되었습니다.", null);
    }

    @Operation(summary = "매니저 정보 수정")
    @ApiErrorCodeExample(domain = ManagerErrorCode.class, value = {"MANAGER_NOT_FOUND", "DELETED_MANAGER"})
    @PutMapping("/{managerId}")
    public ApiResponse<Void> updateManager(
            @PathVariable Long managerId,
            @RequestBody @Valid ManagerUpdateRequest request
    ) {
        log.info("[Manager Update] 계정 정보 수정 요청 - 대상 PK: {}, Role: {}", managerId, request.role());

        UpdateManagerCommand command = new UpdateManagerCommand(
                managerId, request.role(), request.phone(), request.email()
        );
        managerManageUseCase.updateManager(command);

        return ApiResponse.success("MANAGER_UPDATED", "관리자 정보가 수정되었습니다.", null);
    }

    @Operation(summary = "매니저 삭제 (소프트 딜리트)")
    @ApiErrorCodeExample(domain = ManagerErrorCode.class, value = {"MANAGER_NOT_FOUND"})
    @DeleteMapping("/{managerId}")
    public ApiResponse<Void> deleteManager(@PathVariable Long managerId) {
        log.info("[Manager Delete] 계정 삭제 요청 - 대상 PK: {}", managerId);

        managerManageUseCase.deleteManager(managerId);

        return ApiResponse.success("MANAGER_DELETED", "관리자 계정이 삭제 처리되었습니다.", null);
    }

    @Operation(summary = "매니저 전체 조회 및 검색", description = "keyword(아이디 또는 이름)가 없으면 전체를 조회합니다.")
    @GetMapping
    public ApiResponse<List<ManagerResponse>> getManagers(
            @RequestParam(required = false) String keyword
    ) {
        log.info("[Manager List] 계정 조회 요청 - 검색 키워드: {}", keyword);

        List<ManagerResponse> response = managerManageUseCase.getManagers(keyword)
                .stream()
                .map(ManagerResponse::from)
                .toList();

        return ApiResponse.success("MANAGER_LIST_SUCCESS", "매니저 목록 조회 성공", response);
    }
}