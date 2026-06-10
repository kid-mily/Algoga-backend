package com.kidmily.algoga_server.global.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service // 스프링 시큐리티에 등록하기 위한 어노테이션 (도메인 비즈니스 서비스 아님)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글 서버와 통신하여 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 데이터 확인
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        log.info("구글 로그인 시도 - 이메일: {}, 이름: {}", email, name);

        // 3. 어떠한 DB 접근 로직도 없이 스프링 시큐리티 객체 그대로 반환
        return oAuth2User;
    }
}