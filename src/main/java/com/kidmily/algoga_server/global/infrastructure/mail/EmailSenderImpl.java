package com.kidmily.algoga_server.global.infrastructure.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service // @Component와 @Service 중 하나만 있어도 됩니다. @Service를 주로 써요!
@Slf4j
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender javaMailSender;

    // 비밀번호가 제대로 들어오는지 확인하고 싶다면 이렇게 클래스 필드로 받으세요
    @Value("${spring.mail.password}")
    private String password;

    @Async
    @Override
    public void sendEmail(String toAddress, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toAddress);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            log.info("이메일 전송 성공: {}", toAddress);

        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", toAddress, e);
            throw e; // 에러가 나면 컨트롤러에서 알 수 있게 던져주는 게 좋습니다.
        }
    }
}