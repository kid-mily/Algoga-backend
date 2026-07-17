package com.kidmily.algoga_server.chatbot.presentation.internal;

import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotUserDataUseCase;
import com.kidmily.algoga_server.chatbot.presentation.internal.dto.InternalUserData;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Python RAG 서버가 Function Calling 도중 호출하는 내부 전용 API. (API 계약서 구간 C)
 *
 * - 공개 유저 API 와 분리된 /internal/** 경로. X-Internal-Api-Key 헤더로만 접근 가능
 *   ({@link com.kidmily.algoga_server.chatbot.presentation.internal.InternalApiKeyInterceptor}).
 * - 신원(userId)은 Python 이 Spring 에서 받은 값을 그대로 전달한다(모델이 못 바꿈).
 * - 운영에선 내부 네트워크(ECS SG)에서만 접근하도록 인프라에서도 막을 것.
 * - @Hidden: 공개 Swagger 문서에는 노출하지 않는다.
 */
@Hidden
@RestController
@RequestMapping("/internal/chatbot/users/{userId}")
@RequiredArgsConstructor
public class InternalChatbotController {

    private final ChatbotUserDataUseCase chatbotUserDataUseCase;

    @GetMapping("/enrollments")
    public InternalUserData.Enrollments enrollments(@PathVariable Long userId) {
        return chatbotUserDataUseCase.getEnrollments(userId);
    }

    @GetMapping("/payments")
    public InternalUserData.Payments payments(@PathVariable Long userId) {
        return chatbotUserDataUseCase.getPayments(userId);
    }

    @GetMapping("/benefits")
    public InternalUserData.Benefits benefits(@PathVariable Long userId) {
        return chatbotUserDataUseCase.getBenefits(userId);
    }

    @GetMapping("/bookings")
    public InternalUserData.Bookings bookings(@PathVariable Long userId) {
        return chatbotUserDataUseCase.getBookings(userId);
    }
}
