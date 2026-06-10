// chatbot/presentation/api/ChatbotController.java
package com.kidmily.algoga_server.chatbot.presentation.api;

import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotCommandUseCase;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotQueryUseCase;
import com.kidmily.algoga_server.chatbot.presentation.api.request.AskChatbotRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.request.AskSuggestedRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.UnifiedChatHistoryResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
// 🌟 명세해주신 경로 적용
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import com.kidmily.algoga_server.user.domain.User;

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
    @Operation(summary = "통합 채팅방 내역 전체 조회", description = "사용자의 AI 챗봇 대화 이력과 1:1 수동 문의 기록을 결합하여 시간순으로 정렬 조회합니다.")
    public ResponseEntity<ApiResponse<List<UnifiedChatHistoryResponse>>> getChatHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails // 🌟 Security 주입
    ) {
        Long userId = userDetails.getUser().getId(); // 🌟 User 엔티티에서 PK 추출

        List<UnifiedChatHistoryResponse> response = chatbotQueryUseCase.getUnifiedChatHistory(userId);
        return ResponseEntity.ok(ApiResponse.success("UNIFIED_CHAT_HISTORY_LOADED", "통합 채팅 내역 조회 성공", response));
    }

    @GetMapping("/suggested-questions")
    @Operation(summary = "조회용 예상 질문 목록 조회")
    public ResponseEntity<ApiResponse<List<SuggestedQuestionResponse>>> getSuggestedQuestions() {
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTIONS_LOADED", "성공", chatbotQueryUseCase.getSuggestedQuestions()));
    }

    @PostMapping("/suggested-questions/ask")
    @Operation(summary = "조회용 예상 질문 답변 요청")
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> askSuggestedQuestion(@Valid @RequestBody AskSuggestedRequest request) {
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_ANSWERED", "성공", chatbotCommandUseCase.askSuggestedQuestion(request.suggestedQuestionId())));
    }

    @PostMapping("/ask")
    @Operation(summary = "AI 챗봇 직접 입력")
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> askToChatbot(
            @Valid @RequestBody AskChatbotRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails // 🌟 Security 주입
    ) {
        Long userId = userDetails.getUser().getId(); // 🌟 User 엔티티에서 PK 추출
        AskChatbotCommand command = new AskChatbotCommand(userId, request.question());

        ChatbotAnswerResponse responseData = chatbotCommandUseCase.askToChatbot(command);

        if (!responseData.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("CHATBOT_FILTERED", "도메인 외 질문 차단됨", responseData));
        }
        return ResponseEntity.ok(ApiResponse.success("CHATBOT_ANSWERED", "챗봇 답변 완료", responseData));
    }
}