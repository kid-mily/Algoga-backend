package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.RegisterKnowledgeCommand;
import com.kidmily.algoga_server.chatbot.application.command.RegisterSuggestedQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.command.UpdateSuggestedQuestionCommand;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.ExpectedQuery;
import com.kidmily.algoga_server.chatbot.domain.model.Knowledge;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.ExpectedQueryRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.KnowledgeRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatbotAdminCommandService implements ChatbotAdminCommandUseCase {

    private final KnowledgeRepository knowledgeRepository;
    private final ExpectedQueryRepository expectedQueryRepository;
    private final SuggestedQuestionRepository suggestedQuestionRepository;
    private final VectorStore vectorStore;

    // @Lazy: 기동 시 이 서비스 생성으로 인해 vectorStore(Ollama 연동)가 강제 초기화되지 않도록 지연 프록시 주입.
    // 관리자 지식 등록 요청 시점에 초기화되며, Ollama가 없으면 그 요청만 실패하고 서버 기동/기타 기능은 영향 없다.
    public ChatbotAdminCommandService(
            KnowledgeRepository knowledgeRepository,
            ExpectedQueryRepository expectedQueryRepository,
            SuggestedQuestionRepository suggestedQuestionRepository,
            @Lazy @Qualifier("vectorStore") VectorStore vectorStore) {
        this.knowledgeRepository = knowledgeRepository;
        this.expectedQueryRepository = expectedQueryRepository;
        this.suggestedQuestionRepository = suggestedQuestionRepository;
        this.vectorStore = vectorStore;
    }

    @Override
    @Transactional
    public void registerKnowledge(RegisterKnowledgeCommand command) {
        Knowledge savedKnowledge = knowledgeRepository.save(
                Knowledge.create(command.managerId(), command.content())
        );

        List<ExpectedQuery> savedQueries = command.expectedQueries().stream()
                .map(queryText -> ExpectedQuery.create(savedKnowledge.getKnowledgeId(), queryText))
                .map(expectedQueryRepository::save)
                .toList();

        List<Document> documents = savedQueries.stream()
                .map(query -> new Document(
                        String.valueOf(query.getExpectedQueryId()),
                        query.getQueryText(),
                        Map.of(
                                "knowledgeId", savedKnowledge.getKnowledgeId(),
                                "answer", savedKnowledge.getContent()
                        )
                ))
                .collect(Collectors.toList());

        vectorStore.add(documents); 
    }

    @Override
    @Transactional
    public void registerSuggestedQuestion(RegisterSuggestedQuestionCommand command) {
        suggestedQuestionRepository.save(SuggestedQuestion.create(command.question(), command.answer()));
    }

    @Override
    @Transactional
    public void updateSuggestedQuestion(UpdateSuggestedQuestionCommand command) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(command.suggestedQuestionId())
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));
        sq.update(command.question(), command.answer());
        suggestedQuestionRepository.save(sq);
    }

    @Override
    @Transactional
    public void deleteSuggestedQuestion(Long suggestedQuestionId) {
        suggestedQuestionRepository.deleteById(suggestedQuestionId);
    }
}