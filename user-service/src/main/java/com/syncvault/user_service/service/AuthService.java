package com.syncvault.user_service.service;

import com.syncvault.user_service.dto.AuthResponse;
import com.syncvault.user_service.dto.LoginRequest;
import com.syncvault.user_service.dto.RegisterRequest;
import com.syncvault.user_service.dto.RegisterResponse;
import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.exception.DuplicateEmailException;
import com.syncvault.user_service.exception.InvalidCredentialsException;
import com.syncvault.user_service.repository.UserRepository;
import com.syncvault.user_service.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String email;


    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       String email)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.email = email;
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

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .accessToken(token).tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();

    }


}
