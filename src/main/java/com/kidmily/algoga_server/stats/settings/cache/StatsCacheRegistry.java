package com.kidmily.algoga_server.stats.settings.cache;

import com.kidmily.algoga_server.global.cache.CacheRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class StatsCacheRegistry implements CacheRegistry {

    @Override
    public Map<String, Duration> getCacheConfigurations() {
        Map<String, Duration> cacheMap = new HashMap<>();

        for (StatsCacheType cacheType : StatsCacheType.values()) {
            cacheMap.put(
                    cacheType.getCacheName(),
                    Duration.ofSeconds(cacheType.getTtlSeconds())
            );
        }

        return cacheMap;
    }
}
