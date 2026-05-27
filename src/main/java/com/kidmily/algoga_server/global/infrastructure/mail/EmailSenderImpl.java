package com.kidmily.algoga_server.global.infrastructure.mail;

import com.kidmily.algoga_server.user.application.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender javaMailSender;

    @Async // 사용자가 메일이 갈 때까지 기다리지 않도록 비동기 처리
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
        }
    }
}