package com.kidmily.algoga_server.chat.application.port;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatCleanupPort {

    // 소프트 딜리트 후 기준 시각이 지난 채팅방 ID 조회
    List<Long> findExpiredRoomIds(LocalDateTime threshold);

    // 채팅방 관련 데이터 일괄 영구 삭제 (읽음 기록 → 메시지 → 멤버 → 방 순서)
    void hardDeleteRooms(List<Long> roomIds);
}