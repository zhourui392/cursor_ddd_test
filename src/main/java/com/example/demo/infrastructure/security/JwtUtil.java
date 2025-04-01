package com.example.demo.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyNeedsToBeAtLeast32BytesLong}")
    private String secret;

    @Value("${jwt.expiration:86400}") // 默认token有效期为24小时
    private long expiration;
    
    // 使用ConcurrentHashMap存储已注销的token
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * 从token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从token中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从token中提取指定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中提取所有声明
     */
    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否已过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * 检查token是否在黑名单中
     */
    private Boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }
    
    /**
     * 将token加入黑名单
     */
    public void blacklistToken(String token) {
        if (token != null && !token.isEmpty()) {
            try {
                // 获取token的过期时间
                Date expiryDate = extractExpiration(token);
                // 将token加入黑名单，并记录过期时间
                blacklistedTokens.put(token, expiryDate);
                
                // 清理过期的黑名单token
                cleanupBlacklist();
            } catch (Exception e) {
                // 如果token无效，忽略错误
            }
        }
    }
    
    /**
     * 清理黑名单中已过期的token
     */
    private void cleanupBlacklist() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }

    /**
     * 生成token
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        // 检查token是否在黑名单中
        if (isTokenBlacklisted(token)) {
            return false;
        }
        
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
} 