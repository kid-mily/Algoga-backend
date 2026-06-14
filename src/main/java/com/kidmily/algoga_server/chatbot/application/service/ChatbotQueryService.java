// chatbot/application/service/ChatbotQueryService.java
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotQueryUseCase;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.UnifiedChatHistoryResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse; // 🌟 추가
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

        // 1. 최근 챗봇 대화 내용 가져오기 (메모리 보호를 위해 최대 1000개까지만 1차 필터링)
        chatLogRepository.findByUserId(userId, null, 1000).forEach(log -> {
            String status = log.isFiltered() ? "FILTERED" : "COMPLETED";
            unifiedList.add(new UnifiedChatHistoryResponse(
                    "CHAT_" + log.getChatLogId(), "CHATBOT", log.getQuestion(), log.getAnswer(), status, log.getCreatedAt()
            ));
        });

        // 2. 사용자가 남긴 1:1 문의 내역 가져오기
        inquiryRepository.findByUserId(userId).forEach(inq -> {
            unifiedList.add(new UnifiedChatHistoryResponse(
                    "INQ_" + inq.getInquiryId(), "INQUIRY", inq.getQuestion(), inq.getAnswer(), inq.getStatus().name(), inq.getCreatedAt()
            ));
        });

        // 3. 최신 데이터가 0페이지에 오도록 '최신순(내림차순)'으로 먼저 정렬합니다.
        unifiedList.sort(Comparator.comparing(UnifiedChatHistoryResponse::createdAt).reversed());

        // 4. 사용자가 요청한 페이지(page)와 개수(size)만큼 데이터를 잘라냅니다. (메모리 페이징)
        int totalElements = unifiedList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<UnifiedChatHistoryResponse> pagedList = new ArrayList<>();
        if (start < totalElements) {
            pagedList = new ArrayList<>(unifiedList.subList(start, end));
        }

        // 5. 프론트엔드 말풍선은 위(과거)에서 아래(최신)로 그려지므로, 잘라낸 데이터를 다시 '과거순(오름차순)'으로 뒤집어 줍니다.
        Collections.reverse(pagedList);

        return new PageResponse<>(
                pagedList, page, size, totalElements, totalPages, (page == 0), (page >= totalPages - 1)
        );
    }
}