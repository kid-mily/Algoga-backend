package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface ChatLogRepository {
    ChatLog save(ChatLog chatLog);
    List<ChatLog> findByUserId(Long userId, Long beforeChatLogId, int limit);

    /**
     * 어드민용 대화 로그 검색(페이징). 모든 필터는 null 이면 조건을 무시한다.
     * @param isFiltered   true=필터링(차단)된 질문만, false=정상 질문만, null=전체
     * @param from         이 시각 이상(포함). null 이면 하한 없음
     * @param toExclusive  이 시각 미만(제외). null 이면 상한 없음
     * @param keyword      질문/답변 본문에 포함된 키워드. null/blank 면 무시
     */
    Page<ChatLog> searchForAdmin(Boolean isFiltered, Instant from, Instant toExclusive, String keyword, Pageable pageable);
}