package com.kidmily.algoga_server.accommodation.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AccommodationStorageSettings implements StorageSettings {

    private final String bucketName = "algoga-accommodation";
    private final String imageDirectory = "accommodation/images";

    @Override
    public String getDirectory() {
        return "accommodation";
    }
}