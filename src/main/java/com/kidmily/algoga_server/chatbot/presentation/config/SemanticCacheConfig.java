package com.kidmily.algoga_server.chatbot.presentation.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import redis.clients.jedis.JedisPooled;

@Configuration
public class SemanticCacheConfig {

    // Lazy 초기화: RedisVectorStore가 스키마 생성 시 임베딩 차원 조회를 위해 Ollama를 호출한다.
    // 기동 시점에 Ollama/Redis가 없어도 서버가 정상 기동하도록, 실제 사용 시점까지 생성을 미룬다.
    @Bean("semanticCacheStore")
    @Lazy
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