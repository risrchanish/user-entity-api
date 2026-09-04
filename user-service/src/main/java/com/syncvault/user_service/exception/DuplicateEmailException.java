package com.syncvault.user_service.exception;

public class DuplicateEmailException extends RuntimeException{

    public DuplicateEmailException(String message){
        super(message);
    }
}
