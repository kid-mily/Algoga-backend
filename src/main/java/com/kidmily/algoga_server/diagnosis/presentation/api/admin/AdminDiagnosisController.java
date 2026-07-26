package com.kidmily.algoga_server.diagnosis.presentation.api.admin;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.diagnosis.application.command.CreateDiagnosisQuestionCommand;
import com.kidmily.algoga_server.diagnosis.application.command.UpdateDiagnosisQuestionCommand;
import com.kidmily.algoga_server.diagnosis.application.usecase.DiagnosisUseCase;
import com.kidmily.algoga_server.diagnosis.exception.DiagnosisErrorCode;
import com.kidmily.algoga_server.diagnosis.presentation.request.admin.CreateDiagnosisQuestionRequest;
import com.kidmily.algoga_server.diagnosis.presentation.request.admin.UpdateDiagnosisQuestionRequest;
import com.kidmily.algoga_server.diagnosis.presentation.response.AdminDiagnosisQuestionResponse;
import com.kidmily.algoga_server.diagnosis.presentation.response.AdminDiagnosisResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Diagnosis", description = "관리자 진단평가 관리 API")
@RestController
@RequestMapping("/api/v1/admin/diagnosis")
@RequiredArgsConstructor
public class AdminDiagnosisController {

    private final DiagnosisUseCase diagnosisUseCase;

    @Operation(summary = "관리자 진단평가 문항 목록 조회")
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<AdminDiagnosisQuestionResponse>>> getQuestions(
            @RequestParam Long countryId
    ) {
        List<AdminDiagnosisQuestionResponse> response = diagnosisUseCase.getAdminQuestions(countryId)
                .stream()
                .map(AdminDiagnosisQuestionResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_DIAGNOSIS_QUESTIONS_FOUND",
                        "진단평가 문항 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(summary = "관리자 진단평가 문항 등록")
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/questions")
    public ResponseEntity<ApiResponse<AdminDiagnosisQuestionResponse>> createQuestion(
            @Valid @RequestBody CreateDiagnosisQuestionRequest request
    ) {
        var result = diagnosisUseCase.createQuestion(new CreateDiagnosisQuestionCommand(
                request.countryId(),
                request.questionText(),
                request.option1(),
                request.option2(),
                request.option3(),
                request.option4(),
                request.correctOption(),
                request.explanation(),
                request.questionOrder(),
                request.active()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "ADMIN_DIAGNOSIS_QUESTION_CREATED",
                        "진단평가 문항 등록에 성공했습니다.",
                        AdminDiagnosisQuestionResponse.from(result)
                ));
    }

    @Operation(summary = "관리자 진단평가 문항 수정")
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<ApiResponse<AdminDiagnosisQuestionResponse>> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody UpdateDiagnosisQuestionRequest request
    ) {
        var result = diagnosisUseCase.updateQuestion(questionId, new UpdateDiagnosisQuestionCommand(
                request.questionText(),
                request.option1(),
                request.option2(),
                request.option3(),
                request.option4(),
                request.correctOption(),
                request.explanation(),
                request.questionOrder(),
                request.active()
        ));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_DIAGNOSIS_QUESTION_UPDATED",
                        "진단평가 문항 수정에 성공했습니다.",
                        AdminDiagnosisQuestionResponse.from(result)
                )
        );
    }

    @Operation(summary = "관리자 진단평가 결과 목록 조회")
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<PageResponse<AdminDiagnosisResultResponse>>> getResults(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long countryId,

            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<AdminDiagnosisResultResponse> response = diagnosisUseCase.getAdminResults(userId, countryId, pageable)
                .map(AdminDiagnosisResultResponse::from);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_DIAGNOSIS_RESULTS_FOUND",
                        "진단평가 결과 목록 조회에 성공했습니다.",
                        PageResponse.from(response)
                )
        );
    }

    @Operation(summary = "진단평가 문제 즉시 삭제", description = "진단평가 문제와 연결된 답안을 즉시 물리 삭제합니다.")
    @ApiErrorCodeExample(domain = DiagnosisErrorCode.class, value = {"DIAGNOSIS_QUESTION_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @Parameter(description = "진단평가 문제 ID", example = "1")
            @PathVariable Long questionId
    ) {
        diagnosisUseCase.deleteQuestion(questionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "DIAGNOSIS_QUESTION_DELETED",
                        "진단평가 문제 삭제에 성공했습니다."
                )
        );
    }
}
