package com.example.kuby.security.context;

import com.auth0.jwt.interfaces.Claim;
import com.example.kuby.exceptions.BasicException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static com.example.kuby.security.util.parsers.AuthHeaderParser.getPayloadValueByKey;

public class TokenClaimContext {
    private static final ThreadLocal<Map<String, Claim>> tokenClaimThreadLocal = new ThreadLocal<>();

    public static void set(Map<String, Claim> claim) {
        tokenClaimThreadLocal.set(claim);
    }

    public static Map<String, Claim> get() {
        return tokenClaimThreadLocal.get();
    }
    public static void clear() {
        tokenClaimThreadLocal.remove();
    }

    public static UUID getIdByKey(String key) {
        return getPayloadValueByKey(tokenClaimThreadLocal.get(), key, claim -> {
            try {
                return UUID.fromString(claim.asString());
            } catch (IllegalArgumentException ex) {
                throw new BasicException(Map.of(key, "Not matches UUID pattern"), HttpStatus.BAD_REQUEST);
            }
        });
    }
}

