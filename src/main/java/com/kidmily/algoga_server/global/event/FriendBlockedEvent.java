package com.kidmily.algoga_server.global.event;

// 친구 차단 시 발행되는 이벤트. 차단 시 두 사람의 1:1(DIRECT) 채팅방은 삭제하고
// 그룹 채팅방은 유지해야 하는데, 그 처리는 chat 도메인 쪽에서 이 이벤트를 구독해 담당한다.
public record FriendBlockedEvent(Long blockerId, Long blockedId) {}
