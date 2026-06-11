// chatbot/application/service/ChatbotAdminQueryService.java
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.dto.JudgmentQuestionDto;
import com.kidmily.algoga_server.chatbot.application.dto.SuggestedQuestionDto;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminQueryUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.JudgmentQuestion;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.JudgmentQuestionRepository;
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

    private final JudgmentQuestionRepository judgmentQuestionRepository;
    private final SuggestedQuestionRepository suggestedQuestionRepository;

    @Override
    public List<JudgmentQuestionDto> getAllJudgmentQuestions() {
        return judgmentQuestionRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public JudgmentQuestionDto getJudgmentQuestion(Long judgmentQuestionId) {
        JudgmentQuestion jq = judgmentQuestionRepository.findById(judgmentQuestionId)
                // 🌟 수정됨: ChatbotException 교체
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.JUDGMENT_QUESTION_NOT_FOUND));
        return toDto(jq);
    }

    private JudgmentQuestionDto toDto(JudgmentQuestion jq) {
        return new JudgmentQuestionDto(
                jq.getJudgmentQuestionId(), jq.getManagerId(), jq.getQuestion(), jq.getAnswer(), jq.getCreatedAt()
        );
    }

    @Override
    public List<SuggestedQuestionDto> getAllSuggestedQuestions() {
        return suggestedQuestionRepository.findAll().stream()
                .map(sq -> new SuggestedQuestionDto(sq.getSuggestedQuestionId(), sq.getQuestion(), sq.getAnswer()))
                .toList();
    }

    @Override
    public SuggestedQuestionDto getSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(suggestedQuestionId)
                // 🌟 수정됨: ChatbotException 교체
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));
        return new SuggestedQuestionDto(sq.getSuggestedQuestionId(), sq.getQuestion(), sq.getAnswer());
    }
}