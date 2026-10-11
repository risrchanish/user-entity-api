package com.syncvault.user_service.controller;

import com.syncvault.user_service.dto.AddStorageRequest;
import com.syncvault.user_service.dto.UpdateProfileRequest;
import com.syncvault.user_service.dto.UserEmailResponse;
import com.syncvault.user_service.dto.UserProfileResponse;
import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.exception.UnauthorizedInternalCallException;
import com.syncvault.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final String internalApiSecret;

    public UserController(UserService userService,
                          @Value("${internal.api.secret}") String internalApiSecret){
        this.userService = userService;
        this.internalApiSecret = internalApiSecret;
    }

    @GetMapping("{userId}/email")
    public ResponseEntity<UserEmailResponse> getUserEmail(
            @PathVariable("userId") UUID userId,
            @RequestHeader("X-Internal-Api-Key") String providedKey){

        boolean isValidKey = MessageDigest.isEqual(
                providedKey.getBytes(StandardCharsets.UTF_8),
                internalApiSecret.getBytes(StandardCharsets.UTF_8));

        if(!isValidKey){
            throw new UnauthorizedInternalCallException("Invalid Internal API Key");
        }

        User user = userService.findByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(new UserEmailResponse(user.getEmail()));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @PathVariable("userId") UUID userId,
            Authentication authentication){

        verifyOwnership(userId, authentication);
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable("userId") UUID userId,
             @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication){

        verifyOwnership(userId, authentication);
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateProfile(userId, request));
    }

    @PatchMapping("/{userId}/storage")
    public ResponseEntity<Void> addStorage(
            @PathVariable("userId") UUID userId,
            @RequestBody AddStorageRequest request,
            @RequestHeader String providedKey){

        boolean isValidKey = MessageDigest.isEqual(
                providedKey.getBytes(StandardCharsets.UTF_8),
                internalApiSecret.getBytes(StandardCharsets.UTF_8));

        if(!isValidKey){
            throw new UnauthorizedInternalCallException("Invalid internal api key");
        }
        userService.addStorage(userId, request.bytesToAdd());
        return ResponseEntity.noContent().build();
    }

    private void verifyOwnership(UUID requestedUserId, Authentication authentication){
        String authenticatedUserId = authentication.getName();
        if(!authenticatedUserId.equalsIgnoreCase(requestedUserId.toString())){
            throw new AccessDeniedException("Can't access another user's profile");
        }
    }
    
}
