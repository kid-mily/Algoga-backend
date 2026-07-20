package com.kidmily.algoga_server.chatbot.application.port;

import java.util.Optional;

/**
 * chatbot 도메인이 어드민 대화 로그 조회 시 작성자 표시 정보(이름/닉네임)를 얻기 위한 아웃바운드 포트.
 * user 도메인에 대한 컴파일 의존을 두지 않기 위해, 구현체(어댑터)에서 리플렉션으로 user 도메인을 참조한다.
 * (course/benefit/inquiry 도메인의 UserProfilePort 와 동일한 컨벤션)
 */
public interface UserProfilePort {

    Optional<UserProfile> findProfile(Long userId);

    record UserProfile(
            Long userId,
            String name,
            String nickname
    ) {
    }
}
