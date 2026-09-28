package com.syncvault.user_service.service;

import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.exception.UserNotFoundException;
import com.syncvault.user_service.repository.UserRepository;
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
                .orElseThrow(() -> new UserNotFoundException("User not found "+userId));


    }
}
