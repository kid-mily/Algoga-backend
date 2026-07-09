package com.kidmily.algoga_server.passport.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.passport.application.command.RegisterPassportCommand;
import com.kidmily.algoga_server.passport.application.command.UpdatePassportCommand;
import com.kidmily.algoga_server.passport.application.usecase.PassportCommandUseCase;
import com.kidmily.algoga_server.passport.application.usecase.PassportQueryUseCase;
import com.kidmily.algoga_server.passport.domain.model.Passport;
import com.kidmily.algoga_server.passport.domain.model.Sex;
import com.kidmily.algoga_server.passport.exception.PassportErrorCode;
import com.kidmily.algoga_server.passport.presentation.api.request.RegisterPassportRequest;
import com.kidmily.algoga_server.passport.presentation.api.request.UpdatePassportRequest;
import com.kidmily.algoga_server.passport.presentation.api.response.PassportResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/passports")
@RequiredArgsConstructor
@Tag(name = "Passport", description = "여권 정보 도메인 API (암호화 저장)")
public class PassportController {

    private final PassportCommandUseCase passportCommandUseCase;
    private final PassportQueryUseCase passportQueryUseCase;

    @PostMapping
    @Operation(summary = "여권 정보 등록", description = "로그인한 사용자의 여권 정보를 암호화하여 저장합니다. 사용자당 하나만 등록할 수 있습니다.")
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = PassportErrorCode.class, value = {"PASSPORT_ALREADY_EXISTS", "INVALID_EXPIRY_DATE"})
    public ResponseEntity<ApiResponse<PassportResponse>> registerPassport(
            @Valid @RequestBody RegisterPassportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        RegisterPassportCommand command = new RegisterPassportCommand(
                userId,
                request.passportNumber(),
                request.surname(),
                request.givenName(),
                request.nationality(),
                request.issuingCountry(),
                request.birthDate(),
                Sex.fromCode(request.sex()),
                request.issueDate(),
                request.expiryDate(),
                request.personalNumber()
        );

        Passport saved = passportCommandUseCase.register(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("PASSPORT_REGISTERED", "여권 정보 등록에 성공했습니다.", PassportResponse.from(saved)));
    }

    @GetMapping
    @Operation(summary = "내 여권 정보 조회", description = "로그인한 사용자의 여권 정보를 복호화하여 반환합니다.")
    @ApiErrorCodeExample(domain = PassportErrorCode.class, value = {"PASSPORT_NOT_FOUND"})
    public ResponseEntity<ApiResponse<PassportResponse>> getMyPassport(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        Passport passport = passportQueryUseCase.getMyPassport(userId);

        return ResponseEntity.ok(
                ApiResponse.success("PASSPORT_FOUND", "여권 정보 조회에 성공했습니다.", PassportResponse.from(passport))
        );
    }

    @PutMapping
    @Operation(summary = "여권 정보 수정", description = "로그인한 사용자의 기존 여권 정보를 새 값으로 수정합니다.")
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = PassportErrorCode.class, value = {"PASSPORT_NOT_FOUND", "INVALID_EXPIRY_DATE"})
    public ResponseEntity<ApiResponse<PassportResponse>> updatePassport(
            @Valid @RequestBody UpdatePassportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        UpdatePassportCommand command = new UpdatePassportCommand(
                userId,
                request.passportNumber(),
                request.surname(),
                request.givenName(),
                request.nationality(),
                request.issuingCountry(),
                request.birthDate(),
                Sex.fromCode(request.sex()),
                request.issueDate(),
                request.expiryDate(),
                request.personalNumber()
        );

        Passport updated = passportCommandUseCase.update(command);

        return ResponseEntity.ok(
                ApiResponse.success("PASSPORT_UPDATED", "여권 정보 수정에 성공했습니다.", PassportResponse.from(updated))
        );
    }
}
