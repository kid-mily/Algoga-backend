// chatbot/domain/model/JudgmentQuestion.java
package com.kidmily.algoga_server.chatbot.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
public class JudgmentQuestion {
    private Long judgmentQuestionId;
    private Long managerId;
    private String question;
    private String answer;
    private Instant createdAt;

    @Builder
    private JudgmentQuestion(Long judgmentQuestionId, Long managerId, String question, String answer, Instant createdAt) {
        this.judgmentQuestionId = judgmentQuestionId;
        this.managerId = managerId;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
    }

    public static JudgmentQuestion create(Long managerId, String question, String answer) {
        return JudgmentQuestion.builder()
                .managerId(managerId)
                .question(question)
                .answer(answer)
                .createdAt(Instant.now())
                .build();
    }

    // 🌟 추가됨: 내용 수정 비즈니스 로직
    public void update(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public static JudgmentQuestion reconstitute(Long judgmentQuestionId, Long managerId, String question, String answer, Instant createdAt) {
        return JudgmentQuestion.builder()
                .judgmentQuestionId(judgmentQuestionId)
                .managerId(managerId)
                .question(question)
                .answer(answer)
                .createdAt(createdAt)
                .build();
    }
}