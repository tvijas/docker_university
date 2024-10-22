package com.example.kuby.security.repos.token;

import com.example.kuby.foruser.UserEntity;
import com.example.kuby.security.models.entity.tokens.Tokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@EnableRedisRepositories
public interface TokensRepo extends JpaRepository<Tokens, UUID> {
    Optional<Tokens> findByUsers (UserEntity userEntity);
    void deleteByUsers(UserEntity users);
}
