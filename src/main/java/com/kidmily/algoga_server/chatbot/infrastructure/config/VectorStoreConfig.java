package com.kidmily.algoga_server.chatbot.infrastructure.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfig {

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled("localhost", 6379);
    }

    // Lazy 초기화: 이 빈이 실제로 사용될 때까지 생성을 미룬다.
    // RedisVectorStore.afterPropertiesSet()이 스키마 생성을 위해 임베딩 차원을 조회하며
    // Ollama에 실제 호출을 하는데, 기동 시점에 Ollama/Redis가 없으면 컨텍스트 로딩이 실패한다.
    // 지연 초기화로 이 호출을 기동 크리티컬 패스에서 제거하여, 연동 실패 시에도 서버는 정상 기동한다.
    @Bean
    @Lazy
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