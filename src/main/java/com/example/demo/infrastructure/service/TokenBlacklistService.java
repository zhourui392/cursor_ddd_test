package com.example.demo.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 * 使用Redis存储已注销的Token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis中黑名单token的key前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    /**
     * 将token加入黑名单
     *
     * @param token      JWT token
     * @param expiration token过期时间
     */
    public void addToBlacklist(String token, Date expiration) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        try {
            String redisKey = TOKEN_BLACKLIST_PREFIX + token;
            
            // 计算token剩余有效时间（毫秒）
            long expirationTimeMillis = expiration.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long ttl = expirationTimeMillis - currentTimeMillis;
            
            // 只有当token还未过期时才加入黑名单
            if (ttl > 0) {
                log.debug("将token加入黑名单，有效期至: {}", expiration);
                // 将token存入Redis，过期时间设置为token的剩余有效期
                redisTemplate.opsForValue().set(redisKey, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            } else {
                log.debug("token已过期，无需加入黑名单");
            }
        } catch (Exception e) {
            log.error("将token加入黑名单时发生错误", e);
        }
    }
    
    /**
     * 检查token是否在黑名单中
     *
     * @param token JWT token
     * @return 如果token在黑名单中返回true，否则返回false
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        try {
            String redisKey = TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.error("检查token是否在黑名单中时发生错误", e);
            // 发生错误时，为安全起见，将token视为已被拉黑
            return true;
        }
    }
}
