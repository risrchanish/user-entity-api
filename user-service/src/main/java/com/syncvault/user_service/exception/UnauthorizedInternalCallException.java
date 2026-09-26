package com.syncvault.user_service.exception;

public class UnauthorizedInternalCallException extends RuntimeException{

    public UnauthorizedInternalCallException(String message){
        super(message);
    }
}
