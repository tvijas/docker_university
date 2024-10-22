package com.example.kuby.foruser;

import com.example.kuby.foruser.UserEntity;
import com.example.kuby.security.models.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailAndProvider(String email, Provider provider);
    Boolean existsByEmailAndProvider(String email, Provider provider);
    Boolean existsByLogin(String login);
    Optional<UserEntity> findByLogin(String login);
    Optional<UserEntity> findByLoginProviderIdAndProvider(String loginProviderId, Provider provider);
    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.isEmailSubmitted = true WHERE u.email = :email AND u.provider = :provider")
    int updateIsEmailSubmittedByEmailAndProvider(@Param("email") String email, @Param("provider") Provider provider);
    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.login = :login, u.isEmailSubmitted = true WHERE u.email = :email AND u.provider = :provider")
    int updateLoginAndSetEmailSubmittedByEmailAndProvider(@Param("login") String login, @Param("email") String email, @Param("provider") Provider provider);
}
