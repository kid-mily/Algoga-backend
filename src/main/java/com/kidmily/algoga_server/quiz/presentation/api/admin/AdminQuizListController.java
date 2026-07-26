package com.kidmily.algoga_server.quiz.presentation.api.admin;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.quiz.application.usecase.QuizUseCase;
import com.kidmily.algoga_server.quiz.presentation.response.AdminQuizListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Quiz List", description = "콘텐츠 매니저 전체 강의 통합 퀴즈 목록 API")
@RestController
@RequestMapping("/api/v1/admin/quizzes")
@RequiredArgsConstructor
public class AdminQuizListController {

    private final QuizUseCase quizUseCase;

    @Operation(
            summary = "관리자 통합 퀴즈 목록 조회",
            description = "강의별로 나눠 조회하지 않고 전체 강의의 퀴즈를 한 번에 페이지 단위로 조회합니다."
    )
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminQuizListItemResponse>>> getAdminQuizzes(
            @Parameter(description = "강의 ID로 필터링. 미지정 시 전체 강의 대상")
            @RequestParam(required = false) Long courseId,

            @Parameter(description = "퀴즈 문제 검색어")
            @RequestParam(required = false) String keyword,

            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<AdminQuizListItemResponse> response = quizUseCase.getAdminQuizzes(courseId, keyword, pageable)
                .map(AdminQuizListItemResponse::from);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_QUIZZES_FOUND",
                        "관리자 통합 퀴즈 목록 조회에 성공했습니다.",
                        PageResponse.from(response)
                )
        );
    }
}
