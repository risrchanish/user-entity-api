package com.syncvault.user_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RegisterResponse {

    private UUID userId;
    private String email;
    private String fullName;
    private LocalDateTime createdAt;

}
