package com.kidmily.algoga_server.community.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CommunityStorageSettings implements StorageSettings {

    // 🌟 콜론(:) 뒤에 기본값(algoga-marketing-bucket)을 지정합니다.
    // 만약 환경 변수에 community.storage.bucket이 없으면 이 기본값이 자동으로 주입됩니다!
    @Value("${community.storage.bucket:algoga-community}")
    private String bucketName;

    // 🌟 여기도 마찬가지로 환경 변수가 없으면 기본값 'posts'가 주입됩니다.
    @Value("${community.storage.directory:posts}")
    private String directory;
}