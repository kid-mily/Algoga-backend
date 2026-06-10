// chatbot/application/service/ChatbotCommandService.java
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.application.port.out.MainLlmPort;
import com.kidmily.algoga_server.chatbot.application.port.out.PromptFilterPort;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.SuggestedQuestionEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotCommandService implements ChatbotCommandUseCase {

    private final PromptFilterPort promptFilterPort;
    private final MainLlmPort mainLlmPort;
    private final ChatLogRepository chatLogRepository; // 🌟 챗봇 대화 기록 저장소
    private final SuggestedQuestionRepository suggestedQuestionRepository;

    @Override
    @Transactional
    public ChatbotAnswerResponse askToChatbot(AskChatbotCommand command) {
        boolean isValid = promptFilterPort.isValidQuestion(command.question());

        // 1. 필터링된 질문 처리
        if (!isValid) {
            String rejectMessage = "질문의 의도를 정확히 파악하기 어렵습니다. 알고가 서비스와 관련된 내용을 다시 한 번 질문해 주시겠어요?";
            chatLogRepository.save(ChatLog.createFiltered(command.userId(), command.question(), rejectMessage));

            return new ChatbotAnswerResponse(rejectMessage, false);
        }

        // 2. 정상 질문 처리
        String answer = mainLlmPort.generateAnswer(command.question());
        chatLogRepository.save(ChatLog.createNormal(command.userId(), command.question(), answer));

        return new ChatbotAnswerResponse(answer, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatbotAnswerResponse askSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestion suggested = suggestedQuestionRepository.findById(suggestedQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예상 질문입니다."));

        return new ChatbotAnswerResponse(suggested.getAnswer(), true);
    }
}