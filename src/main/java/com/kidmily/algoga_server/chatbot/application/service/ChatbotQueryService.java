// chatbot/application/service/ChatbotQueryService.java
package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotQueryUseCase;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaSuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.presentation.api.response.SuggestedQuestionResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.UnifiedChatHistoryResponse;
import com.kidmily.algoga_server.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotQueryService implements ChatbotQueryUseCase {

    private final SuggestedQuestionRepository suggestedQuestionRepository;
    private final ChatLogRepository chatLogRepository;
    private final InquiryRepository inquiryRepository; // 🌟 1:1 문의 데이터 결합을 위한 포트 의존성

    @Override
    public List<SuggestedQuestionResponse> getSuggestedQuestions() {
        return suggestedQuestionRepository.findAll().stream()
                .map(domain -> new SuggestedQuestionResponse(
                        domain.getSuggestedQuestionId(),
                        domain.getQuestion()
                ))
                .toList();
    }

    @Override
    public List<UnifiedChatHistoryResponse> getUnifiedChatHistory(Long userId) {
        List<UnifiedChatHistoryResponse> unifiedList = new ArrayList<>();

        // 1. AI 챗봇 대화 내용 (ChatLog) 가져오기 및 매핑
        chatLogRepository.findByUserId(userId).forEach(log -> {
            String status = log.isFiltered() ? "FILTERED" : "COMPLETED";
            unifiedList.add(new UnifiedChatHistoryResponse(
                    "CHAT_" + log.getChatLogId(),
                    "CHATBOT",
                    log.getQuestion(),
                    log.getAnswer(),
                    status,
                    log.getCreatedAt()
            ));
        });

        // 2. 관리자 1:1 직접 문의 내용 (Inquiry) 가져오기 및 매핑
        inquiryRepository.findByUserId(userId).forEach(inq -> {
            unifiedList.add(new UnifiedChatHistoryResponse(
                    "INQ_" + inq.getInquiryId(),
                    "INQUIRY",
                    inq.getQuestion(),
                    inq.getAnswer(),
                    inq.getStatus().name(), // Enum 상태값을 문자열(PENDING / ANSWERED)로 매핑
                    inq.getCreatedAt()
            ));
        });

        // 3. 🌟 정렬 타임라인 구성 (시간 오름차순 정렬로 과거 대화부터 최신 대화까지 말풍선 순서 정렬)
        unifiedList.sort(Comparator.comparing(UnifiedChatHistoryResponse::createdAt));

        return unifiedList;
    }
}