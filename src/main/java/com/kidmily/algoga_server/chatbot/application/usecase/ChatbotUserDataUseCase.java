package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.presentation.internal.dto.InternalUserData;

/**
 * Python RAG 서버가 Function Calling 도중 조회하는 회원 데이터 애그리게이터.
 * 각 도메인의 조회 UseCase 를 모아 챗봇용 최소 DTO 로 반환한다.
 */
public interface ChatbotUserDataUseCase {

    InternalUserData.Enrollments getEnrollments(Long userId);

    InternalUserData.Payments getPayments(Long userId);

    InternalUserData.Benefits getBenefits(Long userId);

    InternalUserData.Bookings getBookings(Long userId);
}
