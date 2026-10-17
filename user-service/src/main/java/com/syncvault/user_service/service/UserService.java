package com.syncvault.user_service.service;

import com.syncvault.user_service.dto.UpdateProfileRequest;
import com.syncvault.user_service.dto.UserProfileResponse;
import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.exception.UserNotFoundException;
import com.syncvault.user_service.mapper.UserMapper;
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
                .orElseThrow(() -> new UserNotFoundException("User not found. "+userId));
    }

    public UserProfileResponse getProfile(UUID userId){

        User user = findByUserId(userId);
        return UserMapper.toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request){
        User user = findByUserId(userId);
        user.setFullName(request.getFullName());
        userRepository.save(user);

        return UserMapper.toProfileResponse(user);
    }

    public void addStorage(UUID userId, long bytesToAdd){

        User user = findByUserId(userId);
        user.setStorageUsedBytes(user.getStorageUsedBytes() + bytesToAdd);
        userRepository.save(user);
    }

}
