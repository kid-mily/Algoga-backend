// chatbot/application/service/ChatbotAdminCommandService.java
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.RegisterJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.RegisterSuggestedQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateSuggestedQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.JudgmentQuestion;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.JudgmentQuestionRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatbotAdminCommandService implements ChatbotAdminCommandUseCase {

    private final JudgmentQuestionRepository judgmentQuestionRepository;
    private final SuggestedQuestionRepository suggestedQuestionRepository;

    @Override
    @Transactional
    public void registerJudgmentQuestion(RegisterJudgmentQuestionCommand command) {
        judgmentQuestionRepository.save(JudgmentQuestion.create(command.managerId(), command.question(), command.answer()));
    }

    @Override
    @Transactional
    public void updateJudgmentQuestion(UpdateJudgmentQuestionCommand command) {
        JudgmentQuestion jq = judgmentQuestionRepository.findById(command.judgmentQuestionId())
                // 🌟 수정됨: ChatbotException 교체
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.JUDGMENT_QUESTION_NOT_FOUND));
        jq.update(command.question(), command.answer());
        judgmentQuestionRepository.save(jq);
    }

    @Override
    @Transactional
    public void deleteJudgmentQuestion(Long judgmentQuestionId) {
        judgmentQuestionRepository.deleteById(judgmentQuestionId);
    }

    @Override
    @Transactional
    public void registerSuggestedQuestion(RegisterSuggestedQuestionCommand command) {
        suggestedQuestionRepository.save(SuggestedQuestion.create(command.question(), command.answer()));
    }

    @Override
    @Transactional
    public void updateSuggestedQuestion(UpdateSuggestedQuestionCommand command) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(command.suggestedQuestionId())
                // 🌟 수정됨: ChatbotException 교체
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));
        sq.update(command.question(), command.answer());
        suggestedQuestionRepository.save(sq);
    }

    @Override
    @Transactional
    public void deleteSuggestedQuestion(Long suggestedQuestionId) {
        suggestedQuestionRepository.deleteById(suggestedQuestionId);
    }
}