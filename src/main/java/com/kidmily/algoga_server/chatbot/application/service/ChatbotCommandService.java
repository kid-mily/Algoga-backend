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
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatbotCommandService implements ChatbotCommandUseCase {

    private final PromptFilterPort promptFilterPort;
    private final MainLlmPort mainLlmPort;
    private final ChatLogRepository chatLogRepository;
    private final SuggestedQuestionRepository suggestedQuestionRepository;

    @Override
    @Transactional
    public ChatbotAnswerResponse askToChatbot(AskChatbotCommand command) {
        boolean isValid = promptFilterPort.isValidQuestion(command.question());
        if (!isValid) {
            String rejectMessage = "질문의 의도를 정확히 파악하기 어렵습니다. 알고가 서비스와 관련된 내용을 다시 한 번 질문해 주시겠어요?";
            chatLogRepository.save(ChatLog.createFiltered(command.userId(), command.question(), rejectMessage));
            return new ChatbotAnswerResponse(rejectMessage, false);
        }

        String answer = mainLlmPort.generateAnswer(command.question());
        chatLogRepository.save(ChatLog.createNormal(command.userId(), command.question(), answer));
        return new ChatbotAnswerResponse(answer, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatbotAnswerResponse askSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestion suggested = suggestedQuestionRepository.findById(suggestedQuestionId)
                // 🌟 수정됨: ChatbotException 교체
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));
        return new ChatbotAnswerResponse(suggested.getAnswer(), true);
    }
}