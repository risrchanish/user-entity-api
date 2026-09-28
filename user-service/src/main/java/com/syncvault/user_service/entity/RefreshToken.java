package com.syncvault.user_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String token;
    @ManyToOne
    private User user;
    private Instant expiresAt;
    private Boolean revoked = false;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
