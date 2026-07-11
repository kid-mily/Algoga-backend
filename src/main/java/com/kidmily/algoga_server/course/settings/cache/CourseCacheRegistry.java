package com.kidmily.algoga_server.course.settings.cache;

import com.kidmily.algoga_server.global.cache.CacheRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class CourseCacheRegistry implements CacheRegistry {

    @Override
    public Map<String, Duration> getCacheConfigurations() {
        Map<String, Duration> cacheMap = new HashMap<>();

        for (CourseCacheType cacheType : CourseCacheType.values()) {
            cacheMap.put(
                    cacheType.getCacheName(),
                    Duration.ofSeconds(cacheType.getTtlSeconds())
            );
        }

        return cacheMap;
    }
}