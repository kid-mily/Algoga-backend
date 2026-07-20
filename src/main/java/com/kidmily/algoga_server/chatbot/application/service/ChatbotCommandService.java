package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.application.port.out.RagAnswer;
import com.kidmily.algoga_server.chatbot.application.port.out.RagPort;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import com.kidmily.algoga_server.chatbot.domain.model.RagSourceUsage;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.RagSourceUsageRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 챗봇 오케스트레이터. LLM/임베딩은 직접 호출하지 않고, 검색·생성·함수호출을 모두
 * 외부 Python RAG 서버({@link RagPort})에 위임한다. Spring 은 채팅 로그 저장과 모드 분기만 담당한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotCommandService implements ChatbotCommandUseCase {

    private final RagPort ragPort;
    private final ChatLogRepository chatLogRepository;
    private final SuggestedQuestionRepository suggestedQuestionRepository;
    private final RagSourceUsageRepository ragSourceUsageRepository;

    @Override
    @Transactional
    public ChatbotAnswerResponse askToChatbot(AskChatbotCommand command) {
        String question = command.question();
        Long userId = command.userId();

        // 1. 이전 대화 기록 10개 조회 후, 검색+생성+함수호출을 Python 서버에 위임
        //    (신원 userId 는 서버가 확정해 전달 → 개인 데이터 도구가 이 값으로만 조회)
        List<ChatLog> chatHistory = chatLogRepository.findByUserId(userId, null, 10);
        RagAnswer ragAnswer = ragPort.ask(question, userId, chatHistory);
        String answer = ragAnswer.answer();

        // 2. 도메인 외/형편없는 질문 차단 (Python 이 어떤 도구로도 근거를 못 얻은 경우)
        if (ragAnswer.isRejected()) {
            log.warn("[챗봇 응답] ❌ 서비스 무관 질문으로 판단. 필터링 처리: {}", question);
            chatLogRepository.save(ChatLog.createFiltered(userId, question, answer));
            return ChatbotAnswerResponse.rejected(answer);
        }

        // 3. 채팅 기록 저장 (상담원 연결 포함 정상 흐름은 모두 남긴다)
        ChatLog savedLog = chatLogRepository.save(ChatLog.createNormal(userId, question, answer));

        // 3-1. 답변 근거로 채택된 규정 출처를 기록 → 문서/페이지별 채택 빈도 집계용
        //      (Python 이 답변당 (source, page) 기준 중복 제거해 내려준다)
        if (ragAnswer.sources() != null && !ragAnswer.sources().isEmpty()) {
            List<RagSourceUsage> usages = ragAnswer.sources().stream()
                    .map(s -> RagSourceUsage.create(savedLog.getChatLogId(), s.source(), s.page()))
                    .toList();
            ragSourceUsageRepository.saveAll(usages);
        }

        // 4. 상담원 연결 전환: 프론트가 mode 를 보고 입력 UI 를 상담원 모드로 바꾸고,
        //    요약(handoffSummary)과 원본 문의내용(handoffInquiry)으로 문의 폼을 채운다.
        if (ragAnswer.isHandoff()) {
            log.info("[챗봇 응답] 🙋 상담원 연결 요청 감지. 요약: {}", ragAnswer.handoffSummary());
            return ChatbotAnswerResponse.agentHandoff(
                    answer, ragAnswer.handoffSummary(), ragAnswer.handoffInquiry());
        }

        // 5. 정상 답변
        return ChatbotAnswerResponse.normal(answer);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatbotAnswerResponse askSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(suggestedQuestionId)
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));

        return ChatbotAnswerResponse.normal(sq.getAnswer());
    }
}
