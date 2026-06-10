// chatbot/application/service/ChatbotAdminQueryService.java
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminQueryUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.JudgmentQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.JudgmentQuestionRepository;
import com.kidmily.algoga_server.chatbot.presentation.api.response.JudgmentQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotAdminQueryService implements ChatbotAdminQueryUseCase {

    private final JudgmentQuestionRepository judgmentQuestionRepository;

    @Override
    public List<JudgmentQuestionResponse> getAllJudgmentQuestions() {
        return judgmentQuestionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public JudgmentQuestionResponse getJudgmentQuestion(Long judgmentQuestionId) {
        JudgmentQuestion jq = judgmentQuestionRepository.findById(judgmentQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 지식을 찾을 수 없습니다."));
        return toResponse(jq);
    }

    private JudgmentQuestionResponse toResponse(JudgmentQuestion jq) {
        return new JudgmentQuestionResponse(
                jq.getJudgmentQuestionId(), jq.getManagerId(),
                jq.getQuestion(), jq.getAnswer(), jq.getCreatedAt()
        );
    }
}