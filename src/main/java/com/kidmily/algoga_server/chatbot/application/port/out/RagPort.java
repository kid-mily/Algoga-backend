package com.kidmily.algoga_server.chatbot.application.port.out;

import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;

import java.util.List;

/**
 * RAG(검색+증강+생성) + Function Calling 파이프라인 포트.
 * 실제 검색·생성·개인데이터 조회·상담원 연결 판단은 모두 외부 Python RAG 서버가 수행한다.
 */
public interface RagPort {

    /**
     * 사용자 질문과 이전 대화 이력을 넘겨 답변을 생성한다.
     * 검색·개인데이터 조회·상담원 연결 판단은 모두 Python 서버가 함수호출로 수행한다.
     *
     * @param question    사용자 질문
     * @param userId      인증된 회원 ID(서버가 확정). 개인 데이터 도구가 이 값으로만 조회한다.
     * @param chatHistory 이전 대화 이력(오래된 순). 필터링된 로그는 어댑터가 제외한다.
     * @return 답변·사용도구·모드
     */
    RagAnswer ask(String question, Long userId, List<ChatLog> chatHistory);
}
