package com.syncvault.user_service.service;

import com.syncvault.user_service.config.SecurityConfig;
import com.syncvault.user_service.dto.RegisterRequest;
import com.syncvault.user_service.dto.RegisterResponse;
import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.exception.DuplicateEmailException;
import com.syncvault.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse registerUser(RegisterRequest request){

        boolean userExists = userRepository.findByEmail(request.getEmail()).isPresent();
        if(userExists) {
            throw new DuplicateEmailException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.saveAndFlush(user);


        return RegisterResponse.builder().userId(savedUser.getId())
                .email(savedUser.getEmail()).fullName(savedUser.getFullName())
                .createdAt(savedUser.getCreatedAt()).build();
    }


}
