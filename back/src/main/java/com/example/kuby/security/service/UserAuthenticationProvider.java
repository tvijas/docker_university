package com.example.kuby.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.kuby.exceptions.BasicException;
import com.example.kuby.foruser.UserEntity;
import com.example.kuby.security.models.entity.tokens.Tokens;
import com.example.kuby.security.models.enums.TokenType;
import com.example.kuby.foruser.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.example.kuby.security.util.parsers.AuthHeaderParser.*;


@Service
@RequiredArgsConstructor
public class UserAuthenticationProvider {
    private long accessTokenDurationInSeconds;
    private long refreshTokenDurationInSeconds;
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final Algorithm algorithm;

    @Autowired
    public UserAuthenticationProvider(@Value("${security.jwt.access.token.duration.minutes:15}") long accessDuration,
                                      @Value("${security.jwt.access.token.duration.days:7}") int refreshDuration, JwtService jwtService, UserRepo userRepo, Algorithm algorithm) {
        this.accessTokenDurationInSeconds = Duration.ofMinutes(accessDuration).toSeconds();
        this.refreshTokenDurationInSeconds = Duration.ofDays(refreshDuration).toSeconds();
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.algorithm = algorithm;
    }

    @Transactional
    public String[] refreshTokens(String access_token, String refresh_token) {
        DecodedJWT decodedRefreshToken = validateToken(refresh_token, TokenType.REFRESH);

        if(decodedRefreshToken == null)
            throw new BasicException(Map.of("refresh_token","Refresh token isn't valid"), HttpStatus.UNAUTHORIZED);

        DecodedJWT decodedAccessToken = validateTokenWithoutExp(access_token, TokenType.ACCESS);

        if (!isTokensLinked(decodedAccessToken, decodedRefreshToken))
            throw new BasicException(Map.of("tokens", "Tokens are not linked too each other"), HttpStatus.BAD_REQUEST);

        Map<String, Claim> claims = parsePayloadFromJwt(refresh_token);
        Instant expiresAt = getExpiresAt(claims);

        UserEntity users = userRepo.findByEmailAndProvider(decodedRefreshToken.getSubject(), getProviderFromClaims(claims)).orElseThrow(() ->
                new BasicException(Map.of("refresh_token", "Email from token's subject not found"), HttpStatus.NOT_FOUND));

        String[] accessAndRefreshToken = new String[2];
        try {
            Instant accessTokenExpiration = calculateExpirationInstantWithMicros(accessTokenDurationInSeconds);
            Instant refreshTokenExpiration = calculateExpirationInstantWithMicros(refreshTokenDurationInSeconds);
            Instant updatedAt = Instant.now();

            Tokens tokens = jwtService.refreshTokenPair(expiresAt,
                    updatedAt, accessTokenExpiration,
                    refreshTokenExpiration, users);

            JWTCreator.Builder jwtBuilder = JWT.create();

            Map<String, Claim> accessTokenPayload = decodedAccessToken.getClaims();

            jwtBuilder.withSubject(decodedAccessToken.getSubject());
            accessTokenPayload.forEach((key, value) -> jwtBuilder.withClaim(key, value.asString()));
            jwtBuilder.withExpiresAt(accessTokenExpiration);

            accessAndRefreshToken[0] = jwtBuilder.sign(algorithm);

            accessAndRefreshToken[1] = JWT.create()
                    .withSubject(users.getUsername())
                    .withClaim("userId", users.getId().toString())
                    .withClaim("jwtId", tokens.getRefreshToken().getId().toString())
                    .withClaim("familyId", tokens.getId().toString())
                    .withClaim("tokenType", TokenType.REFRESH.toString())
                    .withClaim("provider", users.getProvider().toString().toLowerCase())
                    .withExpiresAt(refreshTokenExpiration)
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
            throw new JWTCreationException("Error while generating token", exception);
        }
        return accessAndRefreshToken;
    }

    @Transactional
    public String[] generateTokens(UserEntity users) {
        String[] accessAndRefreshToken = new String[2];
        try {
            Instant accessTokenExpiration = calculateExpirationInstantWithMicros(accessTokenDurationInSeconds);
            Instant refreshTokenExpiration = calculateExpirationInstantWithMicros(refreshTokenDurationInSeconds);
            Instant updatedAt = Instant.now();

            Tokens tokens = jwtService.updateTokenPair(updatedAt, accessTokenExpiration, refreshTokenExpiration, users);

            accessAndRefreshToken[0] = JWT.create()
                    .withSubject(users.getUsername())
                    .withClaim("userId", users.getId().toString())
                    .withClaim("jwtId", tokens.getAccessToken().getId().toString())
                    .withClaim("familyId", tokens.getId().toString())
                    .withClaim("tokenType", TokenType.ACCESS.toString())
                    .withClaim("provider", users.getProvider().toString().toUpperCase())
                    .withExpiresAt(accessTokenExpiration)
                    .sign(algorithm);

            accessAndRefreshToken[1] = JWT.create()
                    .withSubject(users.getUsername())
                    .withClaim("userId", users.getId().toString())
                    .withClaim("jwtId", tokens.getRefreshToken().getId().toString())
                    .withClaim("familyId", tokens.getId().toString())
                    .withClaim("tokenType", TokenType.REFRESH.toString())
                    .withClaim("provider", users.getProvider().toString().toUpperCase())
                    .withExpiresAt(refreshTokenExpiration)
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
            throw new JWTCreationException("Error while generating token", exception);
        }
        return accessAndRefreshToken;
    }

    public DecodedJWT validateToken(String token, TokenType expectedTokenType) {
        DecodedJWT decodedJWT = decodeJwt(token);

        if(decodedJWT == null) return null;

        TokenType actualTokenType = TokenType.valueOf(decodedJWT.getClaim("tokenType").asString());

        if (!expectedTokenType.equals(actualTokenType))
            throw new BasicException(Map.of(
                    expectedTokenType.name().toLowerCase() + "_token", "Token type mismatch. " +
                            "Provided - " + actualTokenType +
                            " but expected - " + expectedTokenType), HttpStatus.BAD_REQUEST);

        return decodedJWT;
    }

    public DecodedJWT validateTokenWithoutExp(String token, TokenType expectedTokenType) {
        DecodedJWT decodedJWT = decodeJwtWithoutExp(token);

        TokenType actualTokenType = TokenType.valueOf(decodedJWT.getClaim("tokenType").asString());

        if (!expectedTokenType.equals(actualTokenType))
            throw new BasicException(Map.of(
                    expectedTokenType.name().toLowerCase() + "_token", "Token type mismatch. " +
                            "Provided - " + actualTokenType +
                            " but expected - " + expectedTokenType), HttpStatus.BAD_REQUEST);

        return decodedJWT;
    }

    public Instant calculateExpirationInstantWithMicros(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }

    //    public String addClaimsIdToJwtToken(Map<String, Object> newClaims, String jwt) {
//        try {
//            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
//            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(jwt);
//
//            Map<String, Object> claims = decodedJWT.getClaims().entrySet().stream()
//                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().as(Object.class)));
//
//            claims.putAll(newClaims);
//
//            return JWT.create()
//                    .withSubject(decodedJWT.getSubject())
//                    .withPayload(claims)
//                    .withExpiresAt(decodedJWT.getExpiresAt())
//                    .sign(algorithm);
//        } catch (JWTVerificationException | JWTCreationException exception) {
//            throw new RuntimeException("Error while modifying token", exception);
//        }
//    }
//
    public DecodedJWT decodeJwt(String jwt) {
        try {
            return JWT.require(algorithm).build().verify(jwt);
        } catch (JWTVerificationException ex) {
            return null;
        }
    }

    public DecodedJWT decodeJwtWithoutExp(String jwt) {
        try {
            return JWT.require(algorithm).acceptExpiresAt(Instant.now().plusMillis(1000000).getEpochSecond()).build().verify(jwt);
        } catch (JWTVerificationException ex) {
            throw new RuntimeException("Error while decoding token", ex);
        }
    }

    public boolean isTokensLinked(DecodedJWT decodedAccessToken, DecodedJWT decodedRefreshToken) {
        return decodedAccessToken.getSubject().equals(decodedRefreshToken.getSubject());
    }
}
