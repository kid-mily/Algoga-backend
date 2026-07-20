package com.kidmily.algoga_server;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlgogaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgogaServerApplication.class, args);
    }

    // ECS(Fargate)는 인스턴스 시간대를 UTC로 고정하고 변경할 수 없으므로,
    // 애플리케이션 기동 시 JVM 기본 타임존을 한국 시간(KST)으로 지정한다.
    // 이후 new Date(), LocalDateTime.now(), 로그/JPA Auditing 타임스탬프가 모두 KST 기준으로 동작한다.
    @PostConstruct
    public void setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

}
