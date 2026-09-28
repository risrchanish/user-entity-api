package com.syncvault.user_service.exception;

public class InvalidRefreshTokenException extends RuntimeException{

    public InvalidRefreshTokenException(String message){
        super(message);
    }
}
