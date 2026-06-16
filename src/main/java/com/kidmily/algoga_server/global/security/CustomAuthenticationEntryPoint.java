package com.kidmily.algoga_server.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 1. 프론트엔드에게 상태 코드 401과 JSON 포맷임을 알립니다.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 2. 에러 내용을 프로젝트 공통 형식에 맞게 만듭니다.
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", 401);
        errorDetails.put("code", "UNAUTHORIZED");
        errorDetails.put("message", "로그인이 필요한 서비스입니다. 인증 토큰을 확인해 주세요.");

        // 3. 자바의 Map을 JSON 문자열로 바꿔서 응답으로 쏴줍니다.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }
}