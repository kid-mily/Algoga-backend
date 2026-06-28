package com.kidmily.algoga_server.chatbot.domain.model;
import lombok.Builder; import lombok.Getter; import java.time.Instant;
@Getter
public class Knowledge {
    private final Long knowledgeId; private final Long managerId; private String content; private final Instant createdAt;
    @Builder private Knowledge(Long knowledgeId, Long managerId, String content, Instant createdAt) {
        this.knowledgeId = knowledgeId; this.managerId = managerId; this.content = content; this.createdAt = createdAt;
    }
    public static Knowledge create(Long managerId, String content) {
        return Knowledge.builder().managerId(managerId).content(content).createdAt(Instant.now()).build();
    }
}