// chatbot/presentation/api/ChatbotAdminController.java
package com.kidmily.algoga_server.chatbot.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.chatbot.application.command.RegisterJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminCommandUseCase;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminQueryUseCase;
import com.kidmily.algoga_server.chatbot.presentation.api.request.RegisterJudgmentQuestionRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.request.UpdateJudgmentQuestionRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.response.JudgmentQuestionResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/chatbot/judgment-questions") // 🌟 기본 경로 변경
@RequiredArgsConstructor
@Tag(name = "Chatbot Admin", description = "관리자 전용 챗봇 지식(RAG) CRUD API")
@PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')") // 🌟 클래스 레벨 공통 권한 적용
public class ChatbotAdminController {

    private final ChatbotAdminCommandUseCase commandUseCase;
    private final ChatbotAdminQueryUseCase queryUseCase;

    // 1. [Create] 등록
    @PostMapping
    @Operation(summary = "지식 등록")
    public ResponseEntity<ApiResponse<Void>> registerJudgmentQuestion(
            @Valid @RequestBody RegisterJudgmentQuestionRequest request,
            @CurrentManager Long managerId
    ) {
        commandUseCase.registerJudgmentQuestion(new RegisterJudgmentQuestionCommand(managerId, request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTION_CREATED", "성공적으로 지식이 등록되었습니다.", null));
    }

    // 2. [Read] 전체 목록 조회
    @GetMapping
    @Operation(summary = "지식 목록 전체 조회")
    public ResponseEntity<ApiResponse<List<JudgmentQuestionResponse>>> getAllJudgmentQuestions() {
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTIONS_LOADED", "목록 조회 성공", queryUseCase.getAllJudgmentQuestions()));
    }

    // 3. [Read] 단건 상세 조회
    @GetMapping("/{id}")
    @Operation(summary = "지식 단건 상세 조회")
    public ResponseEntity<ApiResponse<JudgmentQuestionResponse>> getJudgmentQuestion(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTION_LOADED", "상세 조회 성공", queryUseCase.getJudgmentQuestion(id)));
    }

    // 4. [Update] 수정
    @PutMapping("/{id}")
    @Operation(summary = "지식 수정")
    public ResponseEntity<ApiResponse<Void>> updateJudgmentQuestion(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateJudgmentQuestionRequest request
    ) {
        commandUseCase.updateJudgmentQuestion(new UpdateJudgmentQuestionCommand(id, request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTION_UPDATED", "지식이 성공적으로 수정되었습니다.", null));
    }

    // 5. [Delete] 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "지식 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteJudgmentQuestion(@PathVariable("id") Long id) {
        commandUseCase.deleteJudgmentQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTION_DELETED", "지식이 성공적으로 삭제되었습니다.", null));
    }
}