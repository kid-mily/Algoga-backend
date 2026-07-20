package com.kidmily.algoga_server.chatbot.application.dto;

// 규정 문서/페이지별 RAG 채택 빈도 집계 항목
public record RagSourceStatDto(String source, Integer page, long count) {}
