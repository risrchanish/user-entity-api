package com.syncvault.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Must be of at least 8 characters")
    private String password;
}
