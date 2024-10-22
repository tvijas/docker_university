package com.example.kuby.security.service;

import com.example.kuby.exceptions.BasicException;
import com.example.kuby.foruser.UserEntity;
import com.example.kuby.security.models.enums.Provider;
import com.example.kuby.security.models.enums.UserRoles;
import com.example.kuby.foruser.UserRepo;
import com.example.kuby.security.repos.token.TokensRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepo userRepo;
    private final TokensRepo tokensRepo;
    private final PasswordEncoder encoder;

    @Value("${spring.mail.username}")
    private String emailAdmin;

    @Transactional
    public void createUser(String email, String login, String password) {
        if (userRepo.existsByEmailAndProvider(email, Provider.LOCAL))
            throw new BasicException(Map.of("email", "Email is already taken"), HttpStatus.BAD_REQUEST);

        if (userRepo.existsByLogin(login))
            throw new BasicException(Map.of("login", "Login is already taken"), HttpStatus.BAD_REQUEST);

        userRepo.save(UserEntity.builder()
                .email(email)
                .login(login)
                .password(encoder.encode(password))
                .isEmailSubmitted(false)
                .registrationDate(LocalDateTime.now())
                .provider(Provider.LOCAL)
                .roles(UserRoles.USER)
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return userRepo.findByLogin(login).orElseThrow(() ->
                new BasicException(Map.of("login", "User with such login not found"), HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void deleteUserById(UUID userId) {
        UserEntity users = userRepo.findById(userId).orElseThrow(() ->
                new BasicException(Map.of("userId", "User with such id not found"), HttpStatus.NOT_FOUND));
        tokensRepo.deleteByUsers(users);
        userRepo.delete(users);
    }

    @Transactional
    public void changePassword(String email, String password) {
        UserEntity user = userRepo.findByEmailAndProvider(email, Provider.LOCAL).orElseThrow(() ->
                new BasicException(Map.of("email", "User with such email not found"), HttpStatus.NOT_FOUND));

        System.out.println("New password: " + password);

        user.setPassword(encoder.encode(password));

        userRepo.save(user);
    }
}