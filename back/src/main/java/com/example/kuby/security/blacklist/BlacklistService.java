package com.example.kuby.security.blacklist;

import com.example.kuby.security.models.enums.TokenActionType;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BlacklistService {
    private final StringRedisTemplate redisTemplate;

    public BlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String jwtId, Map<String, TokenActionType> actions, long expirationTime) {
        Map<Object, Object> existingActions = redisTemplate.opsForHash().entries(jwtId);
        Map<String, String> updatedActions = new HashMap<>(existingActions.size() + actions.size());

        existingActions.forEach((k, v) -> updatedActions.put((String) k, (String) v));

        actions.forEach((key, actionType) -> {
            String existingAction = updatedActions.get(key);
            if (existingAction == null || actionType.getPriority() > TokenActionType.valueOf(existingAction).getPriority()) {
                updatedActions.put(key, actionType.name());
            }
        });

        if (!updatedActions.isEmpty()) {
            redisTemplate.opsForHash().putAll(jwtId, updatedActions);
            redisTemplate.expire(jwtId, expirationTime, TimeUnit.MILLISECONDS);
        }
    }

    public Map<String, TokenActionType> getBlacklistActions(String jwtId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(jwtId);
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> TokenActionType.valueOf((String) e.getValue()),
                        (v1, v2) -> v2,
                        HashMap::new
                ));
    }

    public void removeFromBlacklist(String jwtId) {
        redisTemplate.delete(jwtId);
    }

    public boolean isBlacklisted(String jwtId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(jwtId));
    }
}