package com.syncvault.user_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;


@Builder
@Getter
public class AuthResponse {

    private UUID userId;
    private String email;
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
