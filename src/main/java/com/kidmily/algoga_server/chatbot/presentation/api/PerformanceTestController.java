package com.kidmily.algoga_server.chatbot.presentation.api;

import com.kidmily.algoga_server.chatbot.application.service.PerformanceTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/perf-test")
@RequiredArgsConstructor
public class PerformanceTestController {

    private final PerformanceTestService testService;

    // 1. 벡터 검색 비교 API
    @PostMapping("/vector/mysql")
    public String mysqlVector(@RequestBody Map<String, String> req) { return testService.testMysqlVector(req.get("question")); }

    @PostMapping("/vector/redis")
    public String redisVector(@RequestBody Map<String, String> req) { return testService.testRedisVector(req.get("question")); }

    // 2. 캐시 성능 비교 API
    @PostMapping("/cache/off")
    public String cacheOff(@RequestBody Map<String, String> req) { return testService.testCacheOff(req.get("question")); }

    @PostMapping("/cache/on")
    public String cacheOn(@RequestBody Map<String, String> req) { return testService.testCacheOn(req.get("question")); }
}