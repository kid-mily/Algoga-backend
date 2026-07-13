package com.kidmily.algoga_server.global.config;

import com.kidmily.algoga_server.global.infrastructure.s3.S3Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client(S3Settings s3Settings) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        var builder = S3Client.builder()
                .region(Region.of(s3Settings.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                // MinIO는 path-style 필수, AWS는 virtual-hosted 기본. 모드에 따라 설정된 값 사용
                .forcePathStyle(s3Settings.isPathStyle());

        // endpoint가 지정된 경우(MinIO/커스텀)에만 override. AWS 모드는 SDK가 리전 엔드포인트를 자동 선택
        if (s3Settings.hasEndpointOverride()) {
            builder.endpointOverride(URI.create(s3Settings.getEndpoint()));
        }

        return builder.build();
    }
}
