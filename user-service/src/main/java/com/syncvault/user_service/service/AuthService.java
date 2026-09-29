package com.syncvault.user_service.service;

import com.syncvault.user_service.dto.AuthResponse;
import com.syncvault.user_service.dto.LoginRequest;
import com.syncvault.user_service.dto.RegisterRequest;
import com.syncvault.user_service.dto.RegisterResponse;
import com.syncvault.user_service.entity.RefreshToken;
import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.exception.DuplicateEmailException;
import com.syncvault.user_service.exception.InvalidCredentialsException;
import com.syncvault.user_service.exception.InvalidRefreshTokenException;
import com.syncvault.user_service.repository.RefreshTokenRepository;
import com.syncvault.user_service.repository.UserRepository;
import com.syncvault.user_service.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;


@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;


    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RegisterResponse registerUser(RegisterRequest request){

        String userEmail = request.getEmail().trim().toLowerCase();
        boolean userExists = userRepository.findByEmail(userEmail).isPresent();
        if(userExists) {
            throw new DuplicateEmailException("Email already registered");
        }

        User user = new User();
        user.setEmail(userEmail);
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.saveAndFlush(user);


        return RegisterResponse.builder().userId(savedUser.getId())
                .email(savedUser.getEmail()).fullName(savedUser.getFullName())
                .createdAt(savedUser.getCreatedAt()).build();
    }

    public AuthResponse loginUser(LoginRequest request){

        String userEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = hashToken(rawRefreshToken);
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(hashedRefreshToken);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setExpiresAt(Instant.now()
                .plusMillis(jwtService.getRefreshTokenExpirationMillis()));
        refreshTokenEntity.setRevoked(false);

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .accessToken(token)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();

    }

    public AuthResponse refreshToken(String rawToken){

        RefreshToken token = findValidRefreshToken(rawToken);

        User user = token.getUser();

        token.setRevoked(true);

        String newAccessToken = jwtService.generateToken(user);
        String newRawRefreshToken = UUID.randomUUID().toString();
        String newHashedRefreshToken = hashToken(newRawRefreshToken);

        RefreshToken newRefreshTokenEntity = new RefreshToken();
        newRefreshTokenEntity.setToken(newHashedRefreshToken);
        newRefreshTokenEntity.setUser(user);
        newRefreshTokenEntity.setExpiresAt(Instant.now()
                .plusMillis(jwtService.getRefreshTokenExpirationMillis()));
        newRefreshTokenEntity.setRevoked(false);

        refreshTokenRepository.save(newRefreshTokenEntity);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .refreshToken(newRawRefreshToken)
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();

    }

    public void logoutUser(String rawToken){

        RefreshToken refreshToken = findValidRefreshToken(rawToken);
        refreshToken.setRevoked(true);
    }

    private RefreshToken findValidRefreshToken(String rawToken){

        String hashToken = hashToken(rawToken);
        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if(Boolean.TRUE.equals(refreshToken.getRevoked()) || refreshToken.getExpiresAt().isBefore(Instant.now())){
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        return refreshToken;
    }

    private String hashToken(String rawToken){

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for(byte b : hashedBytes){
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        }catch(NoSuchAlgorithmException exception){
            throw new RuntimeException("SHA algorithm not available "+exception);
        }
    }

}
