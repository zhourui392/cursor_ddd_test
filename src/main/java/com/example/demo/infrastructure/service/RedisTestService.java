package com.example.demo.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis测试服务
 * 仅在dev环境下启用，用于测试Redis连接
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class RedisTestService implements CommandLineRunner {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void run(String... args) {
        log.info("测试Redis连接...");
        try {
            // 测试Redis连接
            redisTemplate.opsForValue().set("test:key", "test-value");
            Object value = redisTemplate.opsForValue().get("test:key");
            log.info("从Redis获取测试值: {}", value);
            
            // 测试TokenBlacklistService
            String testToken = "test-token";
            tokenBlacklistService.addToBlacklist(testToken, new java.util.Date(System.currentTimeMillis() + 60000));
            boolean isBlacklisted = tokenBlacklistService.isBlacklisted(testToken);
            log.info("测试token是否在黑名单中: {}", isBlacklisted);
            
            log.info("Redis测试成功!");
        } catch (Exception e) {
            log.error("Redis测试失败", e);
        }
    }
}
