package com.kidmily.algoga_server.community.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CommunityStorageSettings implements StorageSettings {

    // object key prefix (예: posts/uuid.png). 버킷은 전역 단일 버킷을 사용한다.
    @Value("${community.storage.directory:posts}")
    private String directory;
}
