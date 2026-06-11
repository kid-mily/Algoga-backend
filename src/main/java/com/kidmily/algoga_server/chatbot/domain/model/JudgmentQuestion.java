// chatbot/domain/model/JudgmentQuestion.java
package com.kidmily.algoga_server.chatbot.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
public class JudgmentQuestion {

    // 식별자와 생성일은 한 번 정해지면 바뀌지 않으므로 final 유지
    private final Long judgmentQuestionId;
    private final Long managerId;
    private final Instant createdAt;

    // 🌟 에러 해결: 값이 수정되어야 하는 필드는 final 키워드를 제거합니다.
    private String question;
    private String answer;

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

    // 🌟 이제 final이 아니므로 에러 없이 정상적으로 값이 수정됩니다.
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