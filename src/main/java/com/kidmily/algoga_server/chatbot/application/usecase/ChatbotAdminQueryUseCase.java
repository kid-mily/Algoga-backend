package com.kidmily.algoga_server.chatbot.application.usecase;

import com.kidmily.algoga_server.chatbot.application.command.SearchChatLogCommand;
import com.kidmily.algoga_server.chatbot.application.dto.AdminChatLogDto;
import com.kidmily.algoga_server.chatbot.application.dto.RagSourceStatDto;
import com.kidmily.algoga_server.chatbot.application.dto.SuggestedQuestionDto;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;

import java.time.Instant;
import java.util.List;

public interface ChatbotAdminQueryUseCase {
    List<SuggestedQuestionDto> getAllSuggestedQuestions();
    SuggestedQuestionDto getSuggestedQuestion(Long suggestedQuestionId);

    // 🌟 어드민 대화 로그 검색(페이징 + 필터링여부/기간/키워드 필터, 작성자 이름·닉네임 포함)
    PageResponse<AdminChatLogDto> getChatLogs(SearchChatLogCommand command);

    // 🌟 규정 문서/페이지별 RAG 채택 빈도 집계(기간 필터, count 내림차순)
    List<RagSourceStatDto> getRagSourceStats(Instant from, Instant toExclusive);
}