package com.kidmily.algoga_server.friend.domain.model;

public enum RelationStatus {
    REQUESTED, // 친구 요청 됨
    ACCEPTED,  // 친구 수락 완료
    REJECTED, // 친구 요청 거절
    BLOCKED    // 차단됨 (요청자=차단한 사람, 수신자=차단당한 사람)
}