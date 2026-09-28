package com.syncvault.user_service.controller;

import com.syncvault.user_service.dto.UserEmailResponse;
import com.syncvault.user_service.entity.User;
import com.syncvault.user_service.exception.UnauthorizedInternalCallException;
import com.syncvault.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    
}
