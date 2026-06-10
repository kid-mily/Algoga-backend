package com.kidmily.algoga_server.chatbot.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
public class ChatLog {
    private Long chatLogId;
    private Long userId;
    private String question;
    private String answer;
    private boolean isFiltered;
    private Instant createdAt;

    @Builder
    private ChatLog(Long chatLogId, Long userId, String question, String answer, boolean isFiltered, Instant createdAt) {
        this.chatLogId = chatLogId;
        this.userId = userId;
        this.question = question;
        this.answer = answer;
        this.isFiltered = isFiltered;
        this.createdAt = createdAt;
    }

    public static ChatLog createNormal(Long userId, String question, String answer) {
        return ChatLog.builder().userId(userId).question(question).answer(answer).isFiltered(false).createdAt(Instant.now()).build();
    }

    public static ChatLog createFiltered(Long userId, String question, String rejectMessage) {
        return ChatLog.builder().userId(userId).question(question).answer(rejectMessage).isFiltered(true).createdAt(Instant.now()).build();
    }

    public static ChatLog reconstitute(Long chatLogId, Long userId, String question, String answer, boolean isFiltered, Instant createdAt) {
        return ChatLog.builder().chatLogId(chatLogId).userId(userId).question(question).answer(answer).isFiltered(isFiltered).createdAt(createdAt).build();
    }
}