package com.example.kuby.security.models.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OauthVerificationRequest {
    @NotBlank
    private final String code;
    @NotBlank
    @Email
    private final String email;
    @NotBlank
    @Size(min = 5,max = 15, message = "Login min size is 5 characters up to max 15 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$",message = "Login must contain only letters from a-z,A-Z and digits from 0-9")
    private final String login;
    @NotBlank
    private final String provider;
}
