package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.RegisterSuggestedQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateSuggestedQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 챗봇 데이터 관리. 현재는 예상 질문 버튼(SuggestedQuestion) CRUD 만 담당한다.
 * (RAG 지식은 Python 서버의 파일 기반 색인으로 이전되어, 기존 Knowledge/ExpectedQuery 등록은 제거됨)
 */
@Service
@RequiredArgsConstructor
public class ChatbotAdminCommandService implements ChatbotAdminCommandUseCase {

    private final SuggestedQuestionRepository suggestedQuestionRepository;

    @Override
    @Transactional
    public void registerSuggestedQuestion(RegisterSuggestedQuestionCommand command) {
        suggestedQuestionRepository.save(SuggestedQuestion.create(command.question(), command.answer()));
    }

    @Override
    @Transactional
    public void updateSuggestedQuestion(UpdateSuggestedQuestionCommand command) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(command.suggestedQuestionId())
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
