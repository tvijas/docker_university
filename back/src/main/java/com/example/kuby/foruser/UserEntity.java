package com.example.kuby.foruser;

import com.example.kuby.security.models.enums.Provider;
import com.example.kuby.security.models.enums.UserRoles;
import com.example.kuby.todolist.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Data
@org.springframework.data.relational.core.mapping.Table(name = "users")
public class UserEntity implements  UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true)
    private String login;
    @Column(nullable = false)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;
    private String loginProviderId;
    private LocalDateTime lastActiveDate;
    private LocalDateTime registrationDate;
    @Column(columnDefinition = "BOOLEAN DEFAULT false",nullable = false)
    private boolean isEmailSubmitted;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRoles roles;
    @OneToMany(mappedBy = "creator",fetch = FetchType.LAZY)
    private List<Task> taskList = new ArrayList<>();
    @PrePersist
    public void prePersist() {
        if (provider == null)
            this.provider = Provider.LOCAL;
        if(roles == null)
            this.roles = UserRoles.USER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == UserRoles.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.email;
    }
    @Override //???????????
    public String getPassword() {
        return this.password;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}