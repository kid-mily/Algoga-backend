package com.kidmily.algoga_server.chatbot.application.command;

import java.time.Instant;

/**
 * 어드민 대화 로그 검색 조건. 모든 필터는 null 이면 무시된다.
 * @param isFiltered  true=차단된 질문만, false=정상 질문만, null=전체
 * @param from        조회 시작 시각(포함). null 이면 하한 없음
 * @param toExclusive 조회 종료 시각(제외, 보통 종료일 다음날 0시). null 이면 상한 없음
 * @param keyword     질문/답변 본문 키워드. null/blank 면 무시
 * @param page        0부터 시작하는 페이지 번호
 */
public record SearchChatLogCommand(
        Boolean isFiltered,
        Instant from,
        Instant toExclusive,
        String keyword,
        int page
) {}
