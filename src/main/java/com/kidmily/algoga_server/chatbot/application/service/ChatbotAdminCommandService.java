// chatbot/application/service/ChatbotAdminCommandService.java
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.RegisterJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateJudgmentQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.JudgmentQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.JudgmentQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatbotAdminCommandService implements ChatbotAdminCommandUseCase {

    private final JudgmentQuestionRepository judgmentQuestionRepository;

    @Override
    @Transactional
    public void registerJudgmentQuestion(RegisterJudgmentQuestionCommand command) {
        judgmentQuestionRepository.save(JudgmentQuestion.create(command.managerId(), command.question(), command.answer()));
    }

    // 🌟 수정 로직 추가
    @Override
    @Transactional
    public void updateJudgmentQuestion(UpdateJudgmentQuestionCommand command) {
        JudgmentQuestion jq = judgmentQuestionRepository.findById(command.judgmentQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("해당 지식을 찾을 수 없습니다."));
        
        // 도메인 객체 상태 변경
        jq.update(command.question(), command.answer());
        
        // 변경 감지(Dirty Checking)가 안되는 어댑터 구조이므로 명시적 save 호출
        judgmentQuestionRepository.save(jq);
    }

    // 🌟 삭제 로직 추가
    @Override
    @Transactional
    public void deleteJudgmentQuestion(Long judgmentQuestionId) {
        judgmentQuestionRepository.deleteById(judgmentQuestionId);
    }
}