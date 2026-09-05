package com.syncvault.user_service.service;

import com.syncvault.user_service.dto.RegisterRequest;
import com.syncvault.user_service.dto.RegisterResponse;
import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.exception.DuplicateEmailException;
import com.syncvault.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_whenEmailAlreadyExists_throwsDuplicateEmailException(){

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        DuplicateEmailException ex = assertThrows(
                DuplicateEmailException.class,
                () -> authService.registerUser(request)
        );
        assertEquals("Email already registered", ex.getMessage());
        verify(userRepository, never()).saveAndFlush(any());

    }

    @Test
    void registerUser_whenValidRequest_returnsPopulatedResponse(){

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setFullName("Test User");
        savedUser.setPasswordHash("hashedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).saveAndFlush(any(User.class));
    }
}
