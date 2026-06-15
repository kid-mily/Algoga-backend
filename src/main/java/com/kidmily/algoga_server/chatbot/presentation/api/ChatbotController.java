// chatbot/presentation/api/ChatbotController.java
package com.kidmily.algoga_server.chatbot.presentation.api;

import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotCommandUseCase;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotQueryUseCase;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode; // 🌟 추가됨
import com.kidmily.algoga_server.chatbot.presentation.api.request.AskChatbotRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.request.AskSuggestedRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.UnifiedChatHistoryResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample; // 🌟 추가됨
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "AI 챗봇 도메인 API")
public class ChatbotController {

    private final ChatbotCommandUseCase chatbotCommandUseCase;
    private final ChatbotQueryUseCase chatbotQueryUseCase;

    @GetMapping("/history")
    @Operation(summary = "통합 채팅방 내역 페이징 조회", description = "가장 최근 대화부터 지정된 개수(기본 20개)만큼 불러옵니다. page=0이 가장 최신 대화입니다.")
    public ResponseEntity<ApiResponse<PageResponse<UnifiedChatHistoryResponse>>> getChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        PageResponse<UnifiedChatHistoryResponse> pagedHistory = chatbotQueryUseCase.getUnifiedChatHistory(userId, page, size);

        return ResponseEntity.ok(ApiResponse.success("UNIFIED_CHAT_HISTORY_LOADED", "성공적으로 채팅 내역을 불러왔습니다.", pagedHistory));
    }

    @GetMapping("/suggested-questions")
    @Operation(summary = "조회용 예상 질문 목록 조회")
    public ResponseEntity<ApiResponse<List<SuggestedQuestionResponse>>> getSuggestedQuestions() {
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTIONS_LOADED", "성공", chatbotQueryUseCase.getSuggestedQuestions()));
    }

    @PostMapping("/suggested-questions/ask")
    @Operation(summary = "조회용 예상 질문 답변 요청")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"}) // 🌟 스웨거 에러 추가
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> askSuggestedQuestion(@Valid @RequestBody AskSuggestedRequest request) {
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_ANSWERED", "성공", chatbotCommandUseCase.askSuggestedQuestion(request.suggestedQuestionId())));
    }

    @PostMapping("/ask")
    @Operation(summary = "AI 챗봇 직접 입력")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {
            "DAILY_USAGE_LIMIT_EXCEEDED",
            "LLM_SERVER_ERROR",
            "VECTOR_DB_ERROR"
    }) // 🌟 스웨거 에러 추가
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> askToChatbot(
            @Valid @RequestBody AskChatbotRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        ChatbotAnswerResponse responseData = chatbotCommandUseCase.askToChatbot(new AskChatbotCommand(userId, request.question()));
        if (!responseData.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("CHATBOT_FILTERED", "도메인 외 질문 차단됨", responseData));
        }
        return ResponseEntity.ok(ApiResponse.success("CHATBOT_ANSWERED", "챗봇 답변 완료", responseData));
    }
}