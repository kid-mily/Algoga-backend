package com.kidmily.algoga_server.lms.presentation.api.admin;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.usecase.DiagnosisUseCase;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Diagnosis", description = "관리자 진단평가 관리 API")
@RestController
@RequestMapping("/api/v1/admin/diagnosis/questions")
@RequiredArgsConstructor
public class AdminDiagnosisController {

    private final DiagnosisUseCase diagnosisUseCase;

    @Operation(summary = "진단평가 문제 즉시 삭제", description = "진단평가 문제와 연결된 답안을 즉시 물리 삭제합니다.")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"DIAGNOSIS_QUESTION_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{questionId}")
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
