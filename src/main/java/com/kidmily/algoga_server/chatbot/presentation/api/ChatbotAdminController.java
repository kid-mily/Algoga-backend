package com.kidmily.algoga_server.chatbot.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.chatbot.application.command.*;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminCommandUseCase;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminQueryUseCase;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.presentation.api.request.*;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
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
@RequestMapping("/api/v1/admin/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot Admin", description = "관리자 전용 챗봇 지식/버튼 관리 API")
@PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ChatbotAdminController {

    private final ChatbotAdminCommandUseCase commandUseCase;
    private final ChatbotAdminQueryUseCase queryUseCase;

    @PostMapping("/knowledges")
    @Operation(summary = "[RAG 지식] 정책/약관 및 예상 질문 일괄 등록", description = "1개의 정책 내용과 N개의 예상 질문을 일괄 등록하고 Vector DB에 동기화합니다.")
    public ResponseEntity<ApiResponse<Void>> registerKnowledge(
            @Valid @RequestBody RegisterKnowledgeRequest request, 
            @CurrentManager Long managerId) {
            
        commandUseCase.registerKnowledge(new RegisterKnowledgeCommand(managerId, request.content(), request.expectedQueries()));
        return ResponseEntity.ok(ApiResponse.success("KNOWLEDGE_CREATED", "성공적으로 정책 지식과 예상 질문이 등록되었습니다.", null));
    }

    @PostMapping("/suggested-questions")
    @Operation(summary = "[버튼 지식] 예상 질문 등록")
    public ResponseEntity<ApiResponse<Void>> registerSuggestedQuestion(@Valid @RequestBody RegisterSuggestedQuestionRequest request) {
        commandUseCase.registerSuggestedQuestion(new RegisterSuggestedQuestionCommand(request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_CREATED", "예상 질문 버튼이 등록되었습니다.", null));
    }

    @GetMapping("/suggested-questions")
    @Operation(summary = "[버튼 지식] 예상 질문 전체 조회 (어드민용)")
    public ResponseEntity<ApiResponse<List<SuggestedQuestionAdminResponse>>> getAllSuggestedQuestions() {
        List<SuggestedQuestionAdminResponse> responses = queryUseCase.getAllSuggestedQuestions().stream()
                .map(dto -> new SuggestedQuestionAdminResponse(dto.suggestedQuestionId(), dto.question(), dto.answer())).toList();
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTIONS_LOADED", "목록 조회 성공", responses));
    }

    @GetMapping("/suggested-questions/{id}")
    @Operation(summary = "[버튼 지식] 예상 질문 단건 조회")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<SuggestedQuestionAdminResponse>> getSuggestedQuestion(@PathVariable("id") Long id) {
        var dto = queryUseCase.getSuggestedQuestion(id);
        var response = new SuggestedQuestionAdminResponse(dto.suggestedQuestionId(), dto.question(), dto.answer());
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_LOADED", "상세 조회 성공", response));
    }

    @PutMapping("/suggested-questions/{id}")
    @Operation(summary = "[버튼 지식] 예상 질문 수정")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> updateSuggestedQuestion(@PathVariable("id") Long id, @Valid @RequestBody UpdateSuggestedQuestionRequest request) {
        commandUseCase.updateSuggestedQuestion(new UpdateSuggestedQuestionCommand(id, request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_UPDATED", "예상 질문이 수정되었습니다.", null));
    }

    @DeleteMapping("/suggested-questions/{id}")
    @Operation(summary = "[버튼 지식] 예상 질문 삭제")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> deleteSuggestedQuestion(@PathVariable("id") Long id) {
        commandUseCase.deleteSuggestedQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_DELETED", "예상 질문이 삭제되었습니다.", null));
    }
}