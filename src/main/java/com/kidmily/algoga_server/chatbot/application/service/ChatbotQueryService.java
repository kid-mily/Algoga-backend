package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotQueryUseCase;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.UnifiedChatHistoryResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotQueryService implements ChatbotQueryUseCase {

    private final SuggestedQuestionRepository suggestedQuestionRepository;
    private final ChatLogRepository chatLogRepository;
    private final InquiryRepository inquiryRepository;

    @Override
    public List<SuggestedQuestionResponse> getSuggestedQuestions() {
        return suggestedQuestionRepository.findAll().stream()
                .map(domain -> new SuggestedQuestionResponse(domain.getSuggestedQuestionId(), domain.getQuestion()))
                .toList();
    }

    @Override
    public PageResponse<UnifiedChatHistoryResponse> getUnifiedChatHistory(Long userId, int page, int size) {
        List<UnifiedChatHistoryResponse> unifiedList = new ArrayList<>();

        chatLogRepository.findByUserId(userId, null, 1000).forEach(log -> {
            String status = log.isFiltered() ? "FILTERED" : "COMPLETED";
            unifiedList.add(new UnifiedChatHistoryResponse(
                    "CHAT_" + log.getChatLogId(), "CHATBOT", log.getQuestion(), log.getAnswer(), status, log.getCreatedAt(),
                    false
            ));
        });

        inquiryRepository.findByUserId(userId).forEach(inq -> {
            // 답변 등록됐지만 사용자가 아직 확인 안 함 → 챗봇 '답변 완료' 뱃지 (inquiry 도메인이 상태 소유)
            unifiedList.add(new UnifiedChatHistoryResponse(
                    "INQ_" + inq.getInquiryId(), "INQUIRY", inq.getQuestion(), inq.getAnswer(), inq.getStatus().name(), inq.getCreatedAt(),
                    inq.isAnswerUnread()
            ));
        });

        unifiedList.sort(Comparator.comparing(UnifiedChatHistoryResponse::createdAt).reversed());

        int totalElements = unifiedList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<UnifiedChatHistoryResponse> pagedList = new ArrayList<>();
        if (start < totalElements) {
            pagedList = new ArrayList<>(unifiedList.subList(start, end));
        }

        Collections.reverse(pagedList);

        return new PageResponse<>(
                pagedList, page, size, totalElements, totalPages, (page == 0), (page >= totalPages - 1)
        );
    }
}