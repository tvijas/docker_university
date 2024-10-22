package com.example.kuby.security.repos.token;

import com.example.kuby.security.models.entity.tokens.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@EnableRedisRepositories
public interface AccessTokenRepo extends JpaRepository<AccessToken, UUID> {
}
