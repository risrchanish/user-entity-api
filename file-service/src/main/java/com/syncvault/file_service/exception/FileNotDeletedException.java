package com.syncvault.file_service.exception;

public class FileNotDeletedException extends RuntimeException{

    public FileNotDeletedException(String message){
        super(message);
    }
}
