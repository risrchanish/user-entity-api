package com.syncvault.file_service.exception;

import java.util.UUID;

public class FileConflictException extends RuntimeException {

    private final UUID conflictCopyId;
    private final String conflictFileName;
    private final Long conflictFileSize;

    public FileConflictException(UUID conflictCopyId, String conflictFileName, Long conflictFileSize){
        super("Conflict detected. A conflict copy has been created.");
        this.conflictCopyId = conflictCopyId;
        this.conflictFileName = conflictFileName;
        this.conflictFileSize = conflictFileSize;
    }

    public UUID getConflictCopyId(){ return conflictCopyId; }
    public String getConflictFileName(){ return conflictFileName; }
    public Long getConflictFileSize(){ return conflictFileSize; }
}