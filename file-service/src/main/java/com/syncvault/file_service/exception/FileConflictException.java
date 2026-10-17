package com.syncvault.file_service.exception;

import java.util.UUID;

public class FileConflictException extends RuntimeException {

    private final UUID conflictCopyId;
    private final String conflictFileName;

    public FileConflictException(UUID conflictCopyId, String conflictFileName){
        super("Conflict detected. A conflict copy has been created.");
        this.conflictCopyId = conflictCopyId;
        this.conflictFileName = conflictFileName;
    }

    public UUID getConflictCopyId(){ return conflictCopyId; }
    public String getConflictFileName(){ return conflictFileName; }
}