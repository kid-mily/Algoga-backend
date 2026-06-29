package com.kidmily.algoga_server.chat.settings.cache;

import com.kidmily.algoga_server.global.cache.CacheRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class ChatCacheRegistry implements CacheRegistry {

    @Override
    public Map<String, Duration> getCacheConfigurations() {
        Map<String, Duration> cacheMap = new HashMap<>();
        for (ChatCacheType cacheType : ChatCacheType.values()) {
            cacheMap.put(cacheType.getCacheName(), Duration.ofSeconds(cacheType.getTtlSeconds()));
        }
        return cacheMap;
    }
}