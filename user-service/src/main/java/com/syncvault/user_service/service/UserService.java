package com.syncvault.user_service.service;

import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public User findByUserId(UUID userId){

        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found "+userId));


    }
}
