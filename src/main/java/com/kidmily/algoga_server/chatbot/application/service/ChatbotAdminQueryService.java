package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.SearchChatLogCommand;
import com.kidmily.algoga_server.chatbot.application.dto.AdminChatLogDto;
import com.kidmily.algoga_server.chatbot.application.dto.RagSourceStatDto;
import com.kidmily.algoga_server.chatbot.application.dto.SuggestedQuestionDto;
import com.kidmily.algoga_server.chatbot.application.port.UserProfilePort;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminQueryUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.RagSourceUsageRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotAdminQueryService implements ChatbotAdminQueryUseCase {

    // 한 페이지 대화 로그 개수
    private static final int CHAT_LOG_PAGE_SIZE = 20;
    // 한 페이지 RAG 채택 통계 개수
    private static final int RAG_STAT_PAGE_SIZE = 20;

    private final SuggestedQuestionRepository suggestedQuestionRepository;
    private final ChatLogRepository chatLogRepository;
    private final RagSourceUsageRepository ragSourceUsageRepository;
    private final UserProfilePort userProfilePort;

    @Override
    public List<SuggestedQuestionDto> getAllSuggestedQuestions() {
        return suggestedQuestionRepository.findAll().stream()
                .map(sq -> new SuggestedQuestionDto(sq.getSuggestedQuestionId(), sq.getQuestion(), sq.getAnswer()))
                .toList();
    }

    @Override
    public SuggestedQuestionDto getSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(suggestedQuestionId)
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));
        return new SuggestedQuestionDto(sq.getSuggestedQuestionId(), sq.getQuestion(), sq.getAnswer());
    }

    @Override
    public PageResponse<AdminChatLogDto> getChatLogs(SearchChatLogCommand command) {
        Pageable pageable = PageRequest.of(command.page(), CHAT_LOG_PAGE_SIZE);

        Page<AdminChatLogDto> dtoPage = chatLogRepository
                .searchForAdmin(command.isFiltered(), command.from(), command.toExclusive(), command.keyword(), pageable)
                .map(this::toAdminChatLogDto);

        return PageResponse.from(dtoPage);
    }

    @Override
    public PageResponse<RagSourceStatDto> getRagSourceStats(Instant from, Instant toExclusive, int page) {
        // 집계 결과(문서/페이지 조합)는 규모가 작아 전체를 뽑아 메모리에서 페이징한다.
        List<RagSourceStatDto> all = getAllRagSourceStats(from, toExclusive);

        int size = RAG_STAT_PAGE_SIZE;
        int total = all.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int fromIdx = Math.min(Math.max(page, 0) * size, total);
        int toIdx = Math.min(fromIdx + size, total);
        List<RagSourceStatDto> content = all.subList(fromIdx, toIdx);

        return new PageResponse<>(
                content, page, size, total, totalPages,
                page == 0, page >= totalPages - 1
        );
    }

    @Override
    public List<AdminChatLogDto> getAllChatLogs(Boolean isFiltered, Instant from, Instant toExclusive, String keyword) {
        return chatLogRepository.searchAllForAdmin(isFiltered, from, toExclusive, keyword).stream()
                .map(this::toAdminChatLogDto)
                .toList();
    }

    @Override
    public List<RagSourceStatDto> getAllRagSourceStats(Instant from, Instant toExclusive) {
        return ragSourceUsageRepository.aggregate(from, toExclusive).stream()
                .map(s -> new RagSourceStatDto(s.source(), s.page(), s.count()))
                .toList();
    }

    // 도메인 ChatLog + 유저 이름/닉네임을 합쳐 어드민 DTO 로 변환.
    private AdminChatLogDto toAdminChatLogDto(ChatLog log) {
        // 탈퇴/삭제 등으로 유저를 못 찾으면 이름/닉네임은 null 로 둔다.
        var profile = userProfilePort.findProfile(log.getUserId()).orElse(null);
        return new AdminChatLogDto(
                log.getChatLogId(), log.getUserId(),
                profile != null ? profile.name() : null,
                profile != null ? profile.nickname() : null,
                log.getQuestion(), log.getAnswer(), log.isFiltered(), log.getCreatedAt()
        );
    }
}