package com.kidmily.algoga_server.packages.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PackageStorageSettings implements StorageSettings {

    private final String bucketName = "algoga-package";
    private final String imageDirectory = "package/images";

    @Override
    public String getDirectory() {
        return "package";
    }
}
