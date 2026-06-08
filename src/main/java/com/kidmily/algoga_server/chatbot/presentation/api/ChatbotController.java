// chatbot/presentation/api/ChatbotController.java
package com.kidmily.algoga_server.chatbot.presentation.api;

// ... 기존 임포트 유지
import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotCommandUseCase;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotQueryUseCase;
import com.kidmily.algoga_server.chatbot.presentation.api.request.AskChatbotRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.request.AskSuggestedRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.request.CreateInquiryRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "AI 챗봇 및 고객센터 도메인 API")
public class ChatbotController {

    private final ChatbotCommandUseCase chatbotCommandUseCase;
    private final ChatbotQueryUseCase chatbotQueryUseCase; // 신규 추가 (조회용)

    // 1. 예상 질문 버튼 목록 조회 (GET)
    @GetMapping("/suggested-questions")
    @Operation(summary = "예상 질문 목록 조회", description = "프론트엔드 챗봇 UI에 띄워줄 질문 버튼 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<SuggestedQuestionResponse>>> getSuggestedQuestions() {
        List<SuggestedQuestionResponse> response = chatbotQueryUseCase.getSuggestedQuestions();
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTIONS_LOADED", "예상 질문 목록을 불러왔습니다.", response));
    }

    // 2. 해당하는 예상 질문 클릭 시 답변 조회 (POST)
    @PostMapping("/suggested-questions/ask")
    @Operation(summary = "예상 질문 답변 요청", description = "선택한 질문에 대응되는 고정 답변을 반환합니다.")
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> askSuggestedQuestion(
            @Valid @RequestBody AskSuggestedRequest request
    ) {
        ChatbotAnswerResponse responseData = chatbotCommandUseCase.askSuggestedQuestion(request.suggestedQuestionId());
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_ANSWERED", "예상 질문 답변이 완료되었습니다.", responseData));
    }

    // 3. AI 봇 직접입력 (기존 로직 유지)
    @PostMapping("/ask")
    @Operation(summary = "AI 챗봇 직접 입력", description = "LLM을 통한 직접 상담을 진행합니다.")
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> askToChatbot(
            @Valid @RequestBody AskChatbotRequest request
    ) {
        Long userId = 1L; // (임시)
        AskChatbotCommand command = new AskChatbotCommand(userId, request.question());
        ChatbotAnswerResponse responseData = chatbotCommandUseCase.askToChatbot(command);

        if (!responseData.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("CHATBOT_FILTERED", "도메인 외 질문으로 필터링되었습니다.", responseData));
        }
        return ResponseEntity.ok(ApiResponse.success("CHATBOT_ANSWERED", "챗봇 답변이 완료되었습니다.", responseData));
    }

    // 4. 사용자 1:1 직접 문의 작성하기 (POST)
    @PostMapping("/inquiries")
    @Operation(summary = "1:1 직접 문의 작성", description = "AI로 해결되지 않은 문제를 사용자가 직접 문의하여 PENDING 상태로 저장합니다.")
    public ResponseEntity<ApiResponse<Void>> createInquiry(
            @Valid @RequestBody CreateInquiryRequest request
    ) {
        Long userId = 1L; // (임시)
        chatbotCommandUseCase.createManualInquiry(userId, request.question());

        return ResponseEntity.ok(ApiResponse.success("INQUIRY_CREATED", "성공적으로 1:1 문의가 접수되었습니다. (상태: PENDING)", null));
    }
}