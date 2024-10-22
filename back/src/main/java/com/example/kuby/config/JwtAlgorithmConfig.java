package com.example.kuby.config;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtAlgorithmConfig {
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String JWT_SECRET;
    @Bean
    public Algorithm getJwtAlgorithm(){
        return Algorithm.HMAC256(JWT_SECRET);
    }
}
