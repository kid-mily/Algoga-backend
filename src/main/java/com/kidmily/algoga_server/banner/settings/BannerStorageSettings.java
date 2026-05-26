package com.kidmily.algoga_server.banner.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings; // 🔥 추가
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BannerStorageSettings implements StorageSettings { // 🔥 인터페이스 구현

    @Value("${banner.storage.bucket}")
    private String bucketName;

    @Value("${banner.storage.directory}")
    private String directory;
}