package com.syncvault.file_service.exception;

public class StorageLimitExceededException extends RuntimeException{

    public StorageLimitExceededException(String message){
        super(message);
    }
}
