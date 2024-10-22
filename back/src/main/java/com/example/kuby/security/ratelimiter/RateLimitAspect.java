package com.example.kuby.security.ratelimiter;

import com.example.kuby.exceptions.BasicException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final StringRedisTemplate redisTemplate;

    @Value("${frontend.server.ip}")
    private String FRONTEND_SERVER_IP;

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    @Before("@annotation(withRateLimitProtection)")
    public void rateLimit(WithRateLimitProtection withRateLimitProtection) {
        String ipAddress = getClientIpAddress();
        if (ipAddress.equals(FRONTEND_SERVER_IP)) return;

        String endpointIdentifier = getEndpointIdentifier();
        String redisKey = RATE_LIMIT_PREFIX + endpointIdentifier + ":" + ipAddress;

        Long requestCount = redisTemplate.opsForValue().increment(redisKey);

        if (requestCount != null) {
            if (requestCount == 1) {
                redisTemplate.expire(redisKey, withRateLimitProtection.rateDuration(), TimeUnit.MILLISECONDS);
            }

            if (requestCount > withRateLimitProtection.rateLimit()) {
                throw new BasicException(Map.of("request","Too many requests"), HttpStatus.TOO_MANY_REQUESTS);
            }
        } else {
            throw new RuntimeException("Failed to increment request count in Redis");
        }
    }

    private String getEndpointIdentifier() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return requestAttributes.getRequest().getRequestURI();
        }
        throw new RuntimeException("Unable to get request attributes");
    }

    private String getClientIpAddress() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return requestAttributes.getRequest().getRemoteAddr();
        }
        throw new RuntimeException("Unable to get request attributes");
    }
}