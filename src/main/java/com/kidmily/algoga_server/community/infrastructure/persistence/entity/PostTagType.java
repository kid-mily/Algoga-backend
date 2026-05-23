package com.kidmily.algoga_server.community.infrastructure.persistence.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostTagType {

    TRAVEL_REVIEW("여행후기"),
    TIP_INFO("팁&정보"),
    QUESTION("질문"),
    COMPANION("동행 구해요"),
    COUNTRY("나라"),
    LECTURE("수강강의"),
    FREE("자유");

    // 한글 명칭을 저장할 변수 (final로 안전하게 보호)
    private final String description;
}