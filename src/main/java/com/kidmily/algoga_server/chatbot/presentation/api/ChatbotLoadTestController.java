package com.kidmily.algoga_server.chatbot.presentation.api;

import com.kidmily.algoga_server.chatbot.application.service.ChatbotLoadTestService;
import com.kidmily.algoga_server.chatbot.presentation.api.request.AskChatbotRequest;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/load-test/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot Load Test", description = "진짜 임베딩 기반 성능 비교 API")
public class ChatbotLoadTestController {

    private final ChatbotLoadTestService loadTestService;

    @PostMapping("/mysql-vector-calc")
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> mysqlTest(@Valid @RequestBody AskChatbotRequest request) {
        return ResponseEntity.ok(ApiResponse.success("TEST_MYSQL", "MySQL 기반 처리 성공", loadTestService.searchFromMysqlWithVectorCalc(request.question())));
    }

    @PostMapping("/redis-vector-search")
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> redisTest(@Valid @RequestBody AskChatbotRequest request) {
        return ResponseEntity.ok(ApiResponse.success("TEST_REDIS", "Redis 기반 검색 성공", loadTestService.searchFromRedisVector(request.question())));
    }
}