package com.kidmily.algoga_server.chatbot.application.port.out;

public interface KnowledgeRetrievalPort {
    String retrieveRelevantKnowledge(String question);
}