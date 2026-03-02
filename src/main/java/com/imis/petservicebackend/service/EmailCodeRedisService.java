package com.imis.petservicebackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailCodeRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "email:code:";

    // 保存验证码（5分钟）
    public void saveCode(String email, String code) {
        redisTemplate.opsForValue()
                .set(PREFIX + email, code, 5, TimeUnit.MINUTES);
    }

    // 校验验证码
    public boolean verifyCode(String email, String code) {
        String cacheCode = redisTemplate.opsForValue()
                .get(PREFIX + email);
        return code != null && code.equals(cacheCode);
    }

    // 验证后删除（更安全）
    public void deleteCode(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}
