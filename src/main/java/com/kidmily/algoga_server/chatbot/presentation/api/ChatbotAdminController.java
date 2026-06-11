// chatbot/presentation/api/ChatbotAdminController.java
package com.kidmily.algoga_server.chatbot.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.chatbot.application.command.*;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminCommandUseCase;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminQueryUseCase;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode; // 🌟 추가됨
import com.kidmily.algoga_server.chatbot.presentation.api.request.*;
import com.kidmily.algoga_server.chatbot.presentation.api.response.*;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample; // 🌟 추가됨
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

    @PostMapping("/judgment-questions")
    @Operation(summary = "[RAG 지식] 지식 등록")
    public ResponseEntity<ApiResponse<Void>> registerJudgmentQuestion(@Valid @RequestBody RegisterJudgmentQuestionRequest request, @CurrentManager Long managerId) {
        commandUseCase.registerJudgmentQuestion(new RegisterJudgmentQuestionCommand(managerId, request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTION_CREATED", "성공적으로 지식이 등록되었습니다.", null));
    }

    @GetMapping("/judgment-questions")
    @Operation(summary = "[RAG 지식] 지식 목록 전체 조회")
    public ResponseEntity<ApiResponse<List<JudgmentQuestionResponse>>> getAllJudgmentQuestions() {
        List<JudgmentQuestionResponse> responses = queryUseCase.getAllJudgmentQuestions().stream()
                .map(dto -> new JudgmentQuestionResponse(dto.judgmentQuestionId(), dto.managerId(), dto.question(), dto.answer(), dto.createdAt())).toList();
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTIONS_LOADED", "목록 조회 성공", responses));
    }

    @GetMapping("/judgment-questions/{id}")
    @Operation(summary = "[RAG 지식] 지식 단건 조회")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"JUDGMENT_QUESTION_NOT_FOUND"}) // 🌟 스웨거 에러
    public ResponseEntity<ApiResponse<JudgmentQuestionResponse>> getJudgmentQuestion(@PathVariable("id") Long id) {
        var dto = queryUseCase.getJudgmentQuestion(id);
        var response = new JudgmentQuestionResponse(dto.judgmentQuestionId(), dto.managerId(), dto.question(), dto.answer(), dto.createdAt());
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTION_LOADED", "상세 조회 성공", response));
    }

    @PutMapping("/judgment-questions/{id}")
    @Operation(summary = "[RAG 지식] 지식 수정")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"JUDGMENT_QUESTION_NOT_FOUND"}) // 🌟 스웨거 에러
    public ResponseEntity<ApiResponse<Void>> updateJudgmentQuestion(@PathVariable("id") Long id, @Valid @RequestBody UpdateJudgmentQuestionRequest request) {
        commandUseCase.updateJudgmentQuestion(new UpdateJudgmentQuestionCommand(id, request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTION_UPDATED", "지식이 성공적으로 수정되었습니다.", null));
    }

    @DeleteMapping("/judgment-questions/{id}")
    @Operation(summary = "[RAG 지식] 지식 삭제")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"JUDGMENT_QUESTION_NOT_FOUND"}) // 🌟 스웨거 에러
    public ResponseEntity<ApiResponse<Void>> deleteJudgmentQuestion(@PathVariable("id") Long id) {
        commandUseCase.deleteJudgmentQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("JUDGMENT_QUESTION_DELETED", "지식이 성공적으로 삭제되었습니다.", null));
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
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"}) // 🌟 스웨거 에러
    public ResponseEntity<ApiResponse<SuggestedQuestionAdminResponse>> getSuggestedQuestion(@PathVariable("id") Long id) {
        var dto = queryUseCase.getSuggestedQuestion(id);
        var response = new SuggestedQuestionAdminResponse(dto.suggestedQuestionId(), dto.question(), dto.answer());
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_LOADED", "상세 조회 성공", response));
    }

    @PutMapping("/suggested-questions/{id}")
    @Operation(summary = "[버튼 지식] 예상 질문 수정")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"}) // 🌟 스웨거 에러
    public ResponseEntity<ApiResponse<Void>> updateSuggestedQuestion(@PathVariable("id") Long id, @Valid @RequestBody UpdateSuggestedQuestionRequest request) {
        commandUseCase.updateSuggestedQuestion(new UpdateSuggestedQuestionCommand(id, request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_UPDATED", "예상 질문이 수정되었습니다.", null));
    }

    @DeleteMapping("/suggested-questions/{id}")
    @Operation(summary = "[버튼 지식] 예상 질문 삭제")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"}) // 🌟 스웨거 에러
    public ResponseEntity<ApiResponse<Void>> deleteSuggestedQuestion(@PathVariable("id") Long id) {
        commandUseCase.deleteSuggestedQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_DELETED", "예상 질문이 삭제되었습니다.", null));
    }
}