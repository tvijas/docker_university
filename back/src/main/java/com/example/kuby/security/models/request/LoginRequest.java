package com.example.kuby.security.models.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    @Valid
    @NotBlank(message = "Login is blank")
    private String login;
    @Valid
    @NotBlank(message = "Password is blank")
    private String password;
}
