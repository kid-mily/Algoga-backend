package com.kidmily.algoga_server.booking.settings.cache;

import com.kidmily.algoga_server.global.cache.CacheRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class BookingCacheRegistry implements CacheRegistry {

    @Override
    public Map<String, Duration> getCacheConfigurations() {
        Map<String, Duration> cacheMap = new HashMap<>();

        for (BookingCacheType cacheType : BookingCacheType.values()) {
            cacheMap.put(
                    cacheType.getCacheName(),
                    Duration.ofSeconds(cacheType.getTtlSeconds())
            );
        }

        return cacheMap;
    }
}
