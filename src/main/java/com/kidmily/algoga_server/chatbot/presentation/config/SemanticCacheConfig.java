package com.kidmily.algoga_server.chatbot.presentation.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class SemanticCacheConfig {

    @Bean("semanticCacheStore")
    public VectorStore semanticCacheStore(EmbeddingModel embeddingModel, JedisPooled jedisPooled) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("algoga-cache-index")
                .prefix("algoga:semantic-cache:") // 🌟 기존 지식과 완벽하게 격리되는 캐시 전용 키 접두사
                .metadataFields(
                        RedisVectorStore.MetadataField.text("cachedAnswer")
                )
                .initializeSchema(true)
                .build();
    }
}