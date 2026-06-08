// chatbot/application/service/ChatbotCommandService.java
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.application.port.out.MainLlmPort;
import com.kidmily.algoga_server.chatbot.application.port.out.PromptFilterPort;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.Inquiry;
import com.kidmily.algoga_server.chatbot.domain.repository.InquiryRepository;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kidmily.algoga_server.chatbot.domain.model.InquiryStatus;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.SuggestedQuestionEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaSuggestedQuestionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotCommandService implements ChatbotCommandUseCase {

    private final PromptFilterPort promptFilterPort;
    private final MainLlmPort mainLlmPort;
    private final InquiryRepository inquiryRepository;
    private final JpaSuggestedQuestionRepository jpaSuggestedQuestionRepository;

    @Override
    @Transactional
    public ChatbotAnswerResponse askToChatbot(AskChatbotCommand command) {

        // 일일 상담 횟수 제한 로직 제거됨

        // 기존 AI 로직 동일하게 유지
        boolean isValid = promptFilterPort.isValidQuestion(command.question());
        if (!isValid) {
            return new ChatbotAnswerResponse("질문의 의도를 정확히 파악하기 어렵습니다. 알고가 서비스와 관련된 내용을 다시 한 번 질문해 주시겠어요?", false);
        }
        String answer = mainLlmPort.generateAnswer(command.question());
        Inquiry inquiry = Inquiry.createCompleted(command.userId(), command.question(), answer);
        inquiryRepository.save(inquiry);

        return new ChatbotAnswerResponse(answer, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatbotAnswerResponse askSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestionEntity suggested = jpaSuggestedQuestionRepository.findById(suggestedQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예상 질문입니다."));

        return new ChatbotAnswerResponse(suggested.getAnswer(), true);
    }

    @Override
    @Transactional
    public void createManualInquiry(Long userId, String question) {
        Inquiry manualInquiry = Inquiry.createPending(userId, question);
        inquiryRepository.save(manualInquiry);
    }
}