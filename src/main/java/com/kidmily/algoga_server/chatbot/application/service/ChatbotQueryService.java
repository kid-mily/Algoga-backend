// chatbot/application/service/ChatbotQueryService.java (신규)
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotQueryUseCase;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaSuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotQueryService implements ChatbotQueryUseCase {

    private final JpaSuggestedQuestionRepository jpaSuggestedQuestionRepository;

    @Override
    public List<SuggestedQuestionResponse> getSuggestedQuestions() {
        return jpaSuggestedQuestionRepository.findAll().stream()
                .map(entity -> new SuggestedQuestionResponse(
                        entity.getSuggestedQuestionId(),
                        entity.getQuestion()
                ))
                .collect(Collectors.toList());
    }
}