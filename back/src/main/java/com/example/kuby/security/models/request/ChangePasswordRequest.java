package com.example.kuby.security.models.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangePasswordRequest {
    @Email
    @NotBlank
    private final String email;
    @NotBlank
    @Size(min = 9, max = 30, message = "Password size should be from 9 to 30 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_-])[A-Za-z\\d@$!%*#?&_-]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private final String password;
}
