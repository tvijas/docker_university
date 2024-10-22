package com.example.kuby.security.service;

import com.example.kuby.foruser.UserEntity;
import com.example.kuby.security.models.enums.UserRoles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    private UserEntity user;
    private Map<String, Object> attributes;
    public CustomOAuth2User(UserEntity user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.user.getRoles() == UserRoles.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    @Override
    public String getName() {
        return user.getEmail();
    }

    public UserEntity getUser() {
        return user;
    }
}

