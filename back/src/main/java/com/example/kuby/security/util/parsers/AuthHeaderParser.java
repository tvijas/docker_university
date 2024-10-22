package com.example.kuby.security.util.parsers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.kuby.exceptions.BasicException;
import com.example.kuby.security.models.enums.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.example.kuby.security.util.parsers.ProviderEnumParser.getProviderFromString;


public class AuthHeaderParser {
    public static UUID parseUserIdFromAuthHeader(String header) {
        String token = recoverToken(header);
        if (token == null) throw new BasicException(Map.of("authHeader", "Invalid Authorization header"),HttpStatus.BAD_REQUEST);
        return UUID.fromString(JWT.decode(token).getClaim("userId").asString());
    }

    public static <T> T getPayloadValueByKey(Map<String, Claim> claims, String key, Function<Claim, T> converter) {
        if (claims == null) {
            throw new IllegalStateException("Token claims not set");
        }
        Claim claim = claims.get(key);
        if (claim == null || claim.isMissing()) {
            throw new BasicException(Map.of(key, "There is no such key in token claims"), HttpStatus.BAD_REQUEST);
        }
        if (claim.isNull()) {
            throw new BasicException(Map.of(key, "Key exists but value is null"),HttpStatus.BAD_REQUEST);
        }
        try {
            return converter.apply(claim);
        } catch (Exception ex) {
            throw new BasicException(Map.of(key, "Value is invalid"),HttpStatus.BAD_REQUEST);
        }
    }

    public static UUID getIdFromClaimsByKey(Map<String, Claim> claims, String key) {
        return getPayloadValueByKey(claims, key, claim -> {
            try {
                return UUID.fromString(claim.asString());
            } catch (IllegalArgumentException ex) {
                throw new BasicException(Map.of(key, "Not matches UUID pattern"),HttpStatus.BAD_REQUEST);
            }
        });
    }
    public static Instant getExpiresAt(Map<String,Claim> claims){
        return getPayloadValueByKey(claims, "expiresAt", claim -> {
            try {
                return claim.asInstant();
            } catch (Exception ex) {
                throw new BasicException(Map.of("expiresAt", "Not matches pattern"), HttpStatus.BAD_REQUEST);
            }
        });
    }
    public static Provider getProviderFromClaims(Map<String,Claim> claims){
        return getPayloadValueByKey(claims,"provider",claim -> getProviderFromString(claim.asString()));
    }

    public static Map<String, Claim> parsePayloadFromJwt(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt);
        Map<String, Claim> claims = new HashMap<>(decodedJWT.getClaims());
        claims.put("expiresAt", new InstantClaim(decodedJWT.getExpiresAtAsInstant()));
        claims.put("email", new StringClaim(decodedJWT.getSubject()));
        return claims;
    }
    public static Map<String, Claim> parsePayloadFromDecodedJwt(DecodedJWT decodedJWT) {
        Map<String, Claim> claims = new HashMap<>(decodedJWT.getClaims());
        claims.put("expiresAt", new InstantClaim(decodedJWT.getExpiresAtAsInstant()));
        claims.put("email", new StringClaim(decodedJWT.getSubject()));
        return claims;
    }

    public static String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return recoverToken(authHeader);
    }

    public static String recoverToken(String headerValue) {
        if (headerValue == null || !headerValue.startsWith("Bearer "))
            return null;
        String token = headerValue.substring(7).trim();

        if (token.isEmpty())
            return null;

        return token;
    }
}
