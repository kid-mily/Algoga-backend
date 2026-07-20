package com.kidmily.algoga_server.chatbot.presentation.api.response;

// 규정 문서/페이지별 RAG 채택 빈도 응답. count 내림차순으로 정렬되어 내려간다.
public record RagSourceStatResponse(String source, Integer page, long count) {}
