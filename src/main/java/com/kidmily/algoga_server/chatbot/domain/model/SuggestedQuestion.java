package com.kidmily.algoga_server.chatbot.domain.model;
import lombok.Builder; import lombok.Getter;
@Getter
public class SuggestedQuestion {
    private final Long suggestedQuestionId; private String question; private String answer;
    @Builder private SuggestedQuestion(Long suggestedQuestionId, String question, String answer) {
        this.suggestedQuestionId = suggestedQuestionId; this.question = question; this.answer = answer;
    }
    public static SuggestedQuestion create(String question, String answer) {
        return SuggestedQuestion.builder().question(question).answer(answer).build();
    }
    public void update(String question, String answer) {
        this.question = question; this.answer = answer;
    }
}