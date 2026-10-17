package com.syncvault.user_service.mapper;

import com.syncvault.user_service.dto.UserProfileResponse;
import com.syncvault.user_service.entity.User;

public class UserMapper {

    private UserMapper(){}
    public static UserProfileResponse toProfileResponse(User user){

        return UserProfileResponse.builder()
                .userId(user.getId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
