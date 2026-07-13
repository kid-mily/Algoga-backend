package com.kidmily.algoga_server.user.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UserStorageSettings implements StorageSettings {

    // object key prefix (예: profiles/uuid.png). 버킷은 전역 단일 버킷을 사용한다.
    @Value("${user.storage.directory:profiles}")
    private String directory;
}
