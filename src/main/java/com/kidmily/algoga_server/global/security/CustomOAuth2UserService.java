package com.kidmily.algoga_server.global.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service // 스프링 시큐리티에 등록하기 위한 어노테이션 (도메인 비즈니스 서비스 아님)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 서버(구글/카카오)와 통신하여 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 어떤 소셜 로그인인지 식별 ("google" or "kakao")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 반환받은 데이터를 우리가 수정할 수 있도록 새로운 Map으로 복사 (불변 객체 에러 방지)
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        // 4. 카카오 로그인일 경우 데이터 평탄화 (껍질 벗기기)
        if ("kakao".equals(registrationId)) {
            // 카카오는 kakao_account 객체 안에 이메일이 들어있음
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            // 카카오는 kakao_account.profile 객체 안에 닉네임(이름)이 들어있음
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            // 구글과 똑같은 Key("email", "name")로 최상단에 꺼내놓음
            attributes.put("email", kakaoAccount.get("email"));
            attributes.put("name", profile.get("nickname"));
        }

        // 평탄화된 데이터 확인용 로그
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        log.info("[{}] 소셜 로그인 시도 - 이메일: {}, 이름: {}", registrationId.toUpperCase(), email, name);

        // 5. 로그인 제공자별로 유저 식별 기준값(PK) 가져오기 (구글="sub", 카카오="id")
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 6. 평탄화가 완료된 속성들(attributes)을 시큐리티 전용 객체에 담아서 반환!
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName
        );
    }
}