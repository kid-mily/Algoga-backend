package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.dto.SuggestedQuestionDto;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminQueryUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotAdminQueryService implements ChatbotAdminQueryUseCase {

    private final SuggestedQuestionRepository suggestedQuestionRepository;

    @Override
    public List<SuggestedQuestionDto> getAllSuggestedQuestions() {
        return suggestedQuestionRepository.findAll().stream()
                .map(sq -> new SuggestedQuestionDto(sq.getSuggestedQuestionId(), sq.getQuestion(), sq.getAnswer()))
                .toList();
    }

    @Override
    public SuggestedQuestionDto getSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(suggestedQuestionId)
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));
        return new SuggestedQuestionDto(sq.getSuggestedQuestionId(), sq.getQuestion(), sq.getAnswer());
    }
}