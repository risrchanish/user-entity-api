package com.syncvault.file_service.service;

import com.syncvault.file_service.entity.FileMetadata;
import com.syncvault.file_service.entity.FileVersion;
import com.syncvault.file_service.repository.FileMetadataRepository;
import com.syncvault.file_service.repository.FileVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ConflictCopyService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileVersionRepository fileVersionRepository;
    private final S3Service s3Service;

    public ConflictCopyService(FileMetadataRepository fileMetadataRepository,
                               FileVersionRepository fileVersionRepository,
                               S3Service s3Service){
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.s3Service = s3Service;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileMetadata createConflictCopy(UUID originalFileId, UUID userId, String originalFileName,
                                           byte[] bytes, String contentType){

        UUID conflictCopyId = UUID.randomUUID();
        String conflictFileName = buildConflictFileName(originalFileName);
        String conflictS3Key = userId + "/conflicts/" + conflictCopyId + "/" + conflictFileName;

        s3Service.uploadFile(conflictS3Key, bytes, contentType);

        FileMetadata conflictCopy = FileMetadata.builder()
                .id(conflictCopyId)
                .userId(userId)
                .fileName(conflictFileName)
                .fileSizeBytes((long) bytes.length)
                .mimeType(contentType)
                .s3Key(conflictS3Key)
                .versionNumber(1)
                .deleted(false)
                .conflictCopy(true)
                .originalFileId(originalFileId)
                .build();
        conflictCopy = fileMetadataRepository.save(conflictCopy);

        FileVersion conflictVersion = FileVersion.builder()
                .fileId(conflictCopyId)
                .userId(userId)
                .createdBy(userId)
                .versionNumber(1)
                .s3Key(conflictS3Key)
                .fileName(conflictFileName)
                .fileSize((long) bytes.length)
                .build();
        fileVersionRepository.save(conflictVersion);

        return conflictCopy;
    }

    private String buildConflictFileName(String originalFileName){
        String base = originalFileName;
        String ext = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if(dotIndex > 0){
            base = originalFileName.substring(0, dotIndex);
            ext = originalFileName.substring(dotIndex);
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return base + "-conflict-" + timestamp + ext;
    }
}