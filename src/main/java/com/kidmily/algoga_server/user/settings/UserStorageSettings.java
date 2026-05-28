package com.kidmily.algoga_server.user.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UserStorageSettings implements StorageSettings {

    @Value("${user.storage.bucket}")
    private String bucketName;

    @Value("${user.storage.directory}")
    private String directory;
}