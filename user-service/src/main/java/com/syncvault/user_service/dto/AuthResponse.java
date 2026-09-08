package com.syncvault.user_service.dto;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
