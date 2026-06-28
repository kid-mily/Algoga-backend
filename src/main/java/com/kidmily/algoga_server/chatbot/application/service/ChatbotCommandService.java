package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.application.port.out.KnowledgeRetrievalPort;
import com.kidmily.algoga_server.chatbot.application.port.out.MainLlmPort;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotCommandService implements ChatbotCommandUseCase {

    private final KnowledgeRetrievalPort knowledgeRetrievalPort;
    private final MainLlmPort mainLlmPort;
    private final ChatLogRepository chatLogRepository;
    private final SuggestedQuestionRepository suggestedQuestionRepository;

    @Override
    @Transactional
    public ChatbotAnswerResponse askToChatbot(AskChatbotCommand command) {
        String question = command.question();
        Long userId = command.userId();

        // 1. 단 한 번의 Vector DB 검색 (필터링 + 약관 추출)
        String relevantKnowledge = knowledgeRetrievalPort.retrieveRelevantKnowledge(question);

        // 2. 관련 약관이 없으면 즉시 차단 메시지 반환
        if (relevantKnowledge == null || relevantKnowledge.isBlank()) {
            log.warn("[챗봇 응답] ❌ 서비스 무관 질문으로 판단. 필터링 처리: {}", question);
            String rejectMessage = "죄송합니다. 저는 알고가 서비스와 관련된 질문(결제, 환불, 코스 안내 등)에만 답변해 드릴 수 있어요.";
            
            chatLogRepository.save(ChatLog.createFiltered(userId, question, rejectMessage));
            return new ChatbotAnswerResponse(rejectMessage, false); 
        }

        // 3. 정상 질문인 경우 이전 대화 기록 10개 조회 (문맥 유지용)
        List<ChatLog> chatHistory = chatLogRepository.findByUserId(userId, null, 10);

        // 4. LLM 답변 생성
        log.info("[챗봇 응답] ✅ 정상 질문 확인. 컨텍스트와 대화 기록을 포함하여 LLM 호출을 시작합니다.");
        String answer = mainLlmPort.generateAnswer(question, relevantKnowledge, chatHistory);

        // 5. 성공적으로 생성된 답변을 DB에 저장
        chatLogRepository.save(ChatLog.createNormal(userId, question, answer));

        // 6. 최종 답변 객체 반환
        return new ChatbotAnswerResponse(answer, true); 
    }

    @Override
    @Transactional(readOnly = true)
    public ChatbotAnswerResponse askSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(suggestedQuestionId)
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));
        
        return new ChatbotAnswerResponse(sq.getAnswer(), true);
    }
}