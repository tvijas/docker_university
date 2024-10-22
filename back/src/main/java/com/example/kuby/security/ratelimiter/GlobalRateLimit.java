package com.example.kuby.security.ratelimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GlobalRateLimit {

    @Value("${global.rate.limit}")
    private int RATE_LIMIT;

    @Value("${global.rate.duration}")
    private long RATE_DURATION;

    @Value("${global.block.duration}")
    private long BLOCK_DURATION;

    @Value("${frontend.server.ip}")
    private String FRONTEND_SERVER_IP;

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "global_rate_limit:";
    private static final String BLOCKED_PREFIX = "global_blocked:";


    public boolean isBlocked(String ipAddress, long currentTime) {
        if (ipAddress.equals(FRONTEND_SERVER_IP)) return false;

        String blockedKey = BLOCKED_PREFIX + ipAddress;
        String unblockTimeStr = redisTemplate.opsForValue().get(blockedKey);

        if (unblockTimeStr != null) {
            long unblockTime = Long.parseLong(unblockTimeStr);
            if (currentTime > unblockTime) {
                redisTemplate.delete(blockedKey);
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean allowRequest(String ipAddress, long currentTime) {
        if (isBlocked(ipAddress, currentTime)) {
            return false;
        }

        String rateLimitKey = RATE_LIMIT_PREFIX + ipAddress;
        Long requestCount = redisTemplate.opsForZSet().zCard(rateLimitKey);

        if (requestCount == null) {
            requestCount = 0L;
        }

        cleanUpRequestCounts(rateLimitKey, currentTime);

        if (requestCount >= RATE_LIMIT) {
            String blockedKey = BLOCKED_PREFIX + ipAddress;
            redisTemplate.opsForValue().set(blockedKey, String.valueOf(currentTime + BLOCK_DURATION), BLOCK_DURATION, TimeUnit.MILLISECONDS);
            return false;
        }

        redisTemplate.opsForZSet().add(rateLimitKey, String.valueOf(currentTime), currentTime);
        redisTemplate.expire(rateLimitKey, RATE_DURATION, TimeUnit.SECONDS);

        return true;
    }

    private void cleanUpRequestCounts(String rateLimitKey, long currentTime) {
        redisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, currentTime - RATE_DURATION);
    }
}
