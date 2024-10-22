package com.example.kuby.utils;

import com.example.kuby.foruser.UserEntity;
import com.example.kuby.security.models.enums.Provider;
import com.example.kuby.security.models.enums.UserRoles;
import com.example.kuby.foruser.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DbUtils {
    private final UserRepo userRepo;
    @Value("${spring.mail.username}")
    private String testEmail;
    public static int userCount = 0;

    public UserEntity createUser() {
        String uniqueEmail = (userCount == 0) ? testEmail : userCount + testEmail;
        String uniqueLogin = (userCount == 0) ? "somelogin" : userCount + "somelogin";
        UserEntity user = UserEntity.builder()
                .email(uniqueEmail)
                .login(uniqueLogin)
                .password(new BCryptPasswordEncoder().encode("18-Bad-Boy-18"))
                .isEmailSubmitted(true)
                .provider(Provider.LOCAL)
                .registrationDate(LocalDateTime.now())
                .roles(UserRoles.USER)
                .build();

        userRepo.save(user);
        userCount++;
        return user;
    }
}
