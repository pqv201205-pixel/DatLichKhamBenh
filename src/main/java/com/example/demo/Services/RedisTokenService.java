package com.example.demo.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    // ================= REFRESH TOKEN =================

    public void saveRefreshToken(
            Integer userId,
            String token){

        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                token,
                Duration.ofDays(7)
        );
    }

    public String getRefreshToken(
            Integer userId){

        return redisTemplate.opsForValue()
                .get("refresh:" + userId);
    }

    public void deleteRefreshToken(
            Integer userId){

        redisTemplate.delete(
                "refresh:" + userId
        );
    }

    // ================= OTP =================

    public void saveOtp(
            Integer userId,
            String otp){

        redisTemplate.opsForValue().set(
                "otp:" + userId,
                otp,
                Duration.ofMinutes(15)
        );
    }

    public String getOtp(
            Integer userId){

        return redisTemplate.opsForValue()
                .get("otp:" + userId);
    }

    public void deleteOtp(
            Integer userId){

        redisTemplate.delete(
                "otp:" + userId
        );
    }
}