package com.syncvault.file_service.exception;

import com.syncvault.file_service.dto.ConflictResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StorageLimitExceededException.class)
    public ResponseEntity<ErrorResponse> storageLimitExceededException(StorageLimitExceededException exception) {

        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> fileNotFoundException(FileNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(exception.getMessage()));

    }

    @ExceptionHandler(FileNotDeletedException.class)
    public ResponseEntity<ErrorResponse> fileNotDeletedException(FileNotDeletedException exception){

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(InvalidFileRequestException.class)
    public ResponseEntity<ErrorResponse> invalidFileRequestException(InvalidFileRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(FileConflictException.class)
    public ResponseEntity<ConflictResponse> fileConflictException(FileConflictException exception){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ConflictResponse(exception.getConflictCopyId(), exception.getConflictFileName(), exception.getMessage()));
    }

    @ExceptionHandler(SearchUnavailableException.class)
    public ResponseEntity<ErrorResponse> searchUnavailableException(SearchUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(exception.getMessage()));
    }
}
