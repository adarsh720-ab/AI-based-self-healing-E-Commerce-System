package com.ecommerce.action.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CooldownGuard {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${action.cooldown.ttl-seconds:300}")
    private long cooldownTtlSeconds;

    /**
     * Redis key pattern: action:cooldown:{serviceId}:{anomalyType}
     */
    private String buildKey(String serviceId, String anomalyType) {
        return "action:cooldown:" + serviceId + ":" + anomalyType;
    }

    public boolean isOnCooldown(String serviceId, String anomalyType) {
        String key = buildKey(serviceId, anomalyType);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Uses setIfAbsent for atomic check-and-set.
     * Prevents duplicate actions from concurrent consumer threads.
     */
    public void setCooldown(String serviceId, String anomalyType) {
        String key = buildKey(serviceId, anomalyType);
        Boolean set = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(cooldownTtlSeconds));

        if (Boolean.TRUE.equals(set)) {
            log.debug("[COOLDOWN] Set | key={} ttl={}s", key, cooldownTtlSeconds);
        } else {
            log.debug("[COOLDOWN] Key already present (race handled) | key={}", key);
        }
    }

    public void clearCooldown(String serviceId, String anomalyType) {
        String key = buildKey(serviceId, anomalyType);
        redisTemplate.delete(key);
        log.info("[COOLDOWN] Cleared | key={}", key);
    }
}
