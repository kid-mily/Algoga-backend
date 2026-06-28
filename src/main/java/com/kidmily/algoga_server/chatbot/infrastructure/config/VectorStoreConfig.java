package com.kidmily.algoga_server.chatbot.infrastructure.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfig {

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled("localhost", 6379);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, JedisPooled jedisPooled) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("algoga-knowledge-index")
                .prefix("algoga:embedding:")
                .metadataFields(
                        RedisVectorStore.MetadataField.text("knowledgeId"),
                        RedisVectorStore.MetadataField.text("answer")
                )
                .initializeSchema(true)
                .build();
    }
}