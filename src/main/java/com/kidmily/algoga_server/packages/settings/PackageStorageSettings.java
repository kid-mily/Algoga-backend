package com.kidmily.algoga_server.packages.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PackageStorageSettings implements StorageSettings {

    // object key prefix. 버킷은 전역 단일 버킷을 사용한다.
    private final String imageDirectory = "package/images";

    @Override
    public String getDirectory() {
        return "package";
    }
}
