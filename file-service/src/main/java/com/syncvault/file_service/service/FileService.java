package com.syncvault.file_service.service;

import com.syncvault.file_service.client.UserStorageClient;
import com.syncvault.file_service.dto.*;
import com.syncvault.file_service.entity.FileMetadata;
import com.syncvault.file_service.entity.FileVersion;
import com.syncvault.file_service.exception.*;
import com.syncvault.file_service.repository.FileEmbeddingRepository;
import com.syncvault.file_service.repository.FileMetadataRepository;
import com.syncvault.file_service.repository.FileVersionRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static com.syncvault.file_service.constant.CommonConstant.STORAGE_LIMIT_BYTES;

@Service
public class FileService {


    private final FileMetadataRepository fileMetadataRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FileEmbeddingRepository fileEmbeddingRepository;
    private final S3Service s3Service;
    private final UserStorageClient userStorageClient;
    private final ConflictCopyService conflictCopyService;

    public FileService(FileMetadataRepository fileMetadataRepository,
                       FileVersionRepository fileVersionRepository,
                       FileEmbeddingRepository fileEmbeddingRepository,
                       S3Service s3Service,
                       UserStorageClient userStorageClient,
                       ConflictCopyService conflictCopyService){

        this.fileMetadataRepository = fileMetadataRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.fileEmbeddingRepository = fileEmbeddingRepository;
        this.s3Service = s3Service;
        this.userStorageClient = userStorageClient;
        this.conflictCopyService = conflictCopyService;
    }

    @Transactional
    public FileUploadResponse uploadFile(UUID userId, MultipartFile file){

        long fileSize = file.getSize();

        Long currentUsage = fileMetadataRepository.sumFileSizeByUserId(userId);

        if(currentUsage == null){currentUsage = 0L;}

        if(currentUsage + fileSize > STORAGE_LIMIT_BYTES){
            throw new StorageLimitExceededException("Storage limit of 5 GB exceeded");
        }

        UUID fileId = UUID.randomUUID();
        int versionNumber = 1;
        String s3Key = s3Service.buildS3Key(userId, fileId, versionNumber, file.getOriginalFilename());

        byte[] bytes;
        try{
            bytes = file.getBytes();
        }catch(IOException ex){
            throw new UncheckedIOException(ex);
        }

        s3Service.uploadFile(s3Key, bytes, file.getContentType());

        FileMetadata fileMetadata = FileMetadata.builder()
                .id(fileId)
                .userId(userId)
                .fileName(file.getOriginalFilename())
                .fileSizeBytes(fileSize)
                .mimeType(file.getContentType())
                .s3Key(s3Key)
                .versionNumber(versionNumber)
                .deleted(false)
                .conflictCopy(false)
                .build();
        fileMetadata = fileMetadataRepository.save(fileMetadata);

        FileVersion fileVersion = FileVersion.builder()
                .fileId(fileId)
                .userId(userId)
                .createdBy(userId)
                .versionNumber(versionNumber)
                .s3Key(s3Key)
                .fileName(file.getOriginalFilename())
                .fileSize(fileSize)
                .build();
        fileVersionRepository.save(fileVersion);

        userStorageClient.addStorage(userId, fileSize);

        return new FileUploadResponse(
                fileId,
                fileMetadata.getFileName(),
                fileMetadata.getFileSizeBytes(),
                fileMetadata.getMimeType(),
                fileMetadata.getVersionNumber(),
                fileMetadata.getS3Key(),
                fileMetadata.getCreatedAt());
    }


    @Transactional
    public FileUpdateResponse reUploadFile(UUID userId, UUID fileId, MultipartFile file, Integer expectedVersion){

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .filter(fm -> fm.getUserId().equals(userId))
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        if(Boolean.TRUE.equals(fileMetadata.getDeleted())){
            throw new FileNotFoundException("File not found.");
        }

        if(Boolean.TRUE.equals(fileMetadata.getConflictCopy())){
            throw new InvalidFileRequestException("Conflict copies cannot be re-uploaded.");
        }

        byte[] bytes;
        try{
            bytes = file.getBytes();
        }catch(IOException ex){
            throw new UncheckedIOException(ex);
        }

        // AC-014-01/03: client already knows it's behind -- fork before touching the original at all.
        if(expectedVersion != null && fileMetadata.getVersionNumber() > expectedVersion){
            FileMetadata conflictCopy = conflictCopyService.createConflictCopy(
                    fileMetadata.getId(), userId, file.getOriginalFilename(), bytes, file.getContentType());
            throw new FileConflictException(conflictCopy.getId(), conflictCopy.getFileName());
        }

        long oldFileSize = fileMetadata.getFileSizeBytes();
        long newFileSize = file.getSize();
        long sizeDelta = newFileSize - oldFileSize;

        Long currentUsage = fileMetadataRepository.sumFileSizeByUserId(userId);
        if(currentUsage == null){currentUsage = 0L;}

        if(currentUsage + sizeDelta > STORAGE_LIMIT_BYTES){
            throw new StorageLimitExceededException("Storage limit of 5 GB exceeded");
        }

        int newVersionNumber = fileMetadata.getVersionNumber() + 1;
        String newS3Key = s3Service.buildS3Key(userId, fileId, newVersionNumber, file.getOriginalFilename());

        s3Service.uploadFile(newS3Key, bytes, file.getContentType());

        fileMetadata.setFileName(file.getOriginalFilename());
        fileMetadata.setFileSizeBytes(newFileSize);
        fileMetadata.setS3Key(newS3Key);
        fileMetadata.setVersionNumber(newVersionNumber);
        fileMetadata.setAiSummary(null);
        fileMetadata.setAiDescription(null);
        fileMetadata.setAiTags(null);
        fileMetadata.setAiProcessedAt(null);

        try{
            fileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        }catch(ObjectOptimisticLockingFailureException ex){
            // Genuinely simultaneous race: someone else committed between our read and our write.
            FileMetadata conflictCopy = conflictCopyService.createConflictCopy(
                    fileId, userId, file.getOriginalFilename(), bytes, file.getContentType());
            throw new FileConflictException(conflictCopy.getId(), conflictCopy.getFileName());
        }

        FileVersion fileVersion = FileVersion.builder()
                .fileId(fileId)
                .userId(userId)
                .createdBy(userId)
                .versionNumber(newVersionNumber)
                .s3Key(newS3Key)
                .fileName(file.getOriginalFilename())
                .fileSize(newFileSize)
                .build();
        fileVersionRepository.save(fileVersion);

        userStorageClient.addStorage(userId, sizeDelta);

        return new FileUpdateResponse(
                fileId,
                fileMetadata.getFileName(),
                fileMetadata.getFileSizeBytes(),
                fileMetadata.getVersionNumber(),
                fileMetadata.getS3Key(),
                fileMetadata.getUpdatedAt());
    }

    public List<FileVersionResponse> getFileVersions(UUID userId, UUID fileId){

        fileMetadataRepository.findById(fileId)
                .filter(fm -> fm.getUserId().equals(userId))
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        return fileVersionRepository.findByFileIdOrderByVersionNumberAsc(fileId).stream()
                .map(v -> new FileVersionResponse(
                        v.getId(),
                        v.getVersionNumber(),
                        v.getS3Key(),
                        v.getFileName(),
                        v.getFileSize(),
                        v.getCreatedBy(),
                        v.getCreatedAt()))
                .toList();
    }

    public FileResponse getFile(UUID userId, UUID fileId){
        FileMetadata fm = fileMetadataRepository.findByIdAndUserIdAndDeletedFalse(fileId, userId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));
        return toFileResponse(fm);
    }

    public List<FileResponse> listUserFiles(UUID authenticatedUserId, UUID pathUserId){
        if(!authenticatedUserId.equals(pathUserId)){
            throw new AccessDeniedException("Can't access another user's files");
        }
        return fileMetadataRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(pathUserId).stream()
                .map(this::toFileResponse)
                .toList();
    }

    public FileDownloadResponse getDownloadUrl(UUID userId, UUID fileId){
        FileMetadata fm = fileMetadataRepository.findByIdAndUserIdAndDeletedFalse(fileId, userId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));
        String url = s3Service.generatePresignedDownloadUrl(fm.getS3Key());
        long expirySeconds = s3Service.getPresignedUrlExpiryMinutes() * 60L;
        return new FileDownloadResponse(url, expirySeconds);
    }

    @Transactional
    public void deleteFile(UUID userId, UUID fileId){

        FileMetadata fm = fileMetadataRepository.findByIdAndUserIdAndDeletedFalse(fileId,userId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        fm.setDeleted(true);
        fm.setDeletedAt(LocalDateTime.now());
        fileMetadataRepository.save(fm);
        // Kafka events goes here.

    }

    @Transactional
    public FileResponse restoreFile(UUID userId, UUID fileId){

        FileMetadata fm = fileMetadataRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        if(!Boolean.TRUE.equals(fm.getDeleted())){
            throw new FileNotDeletedException("File is not deleted.");
        }

        fm.setDeleted(false);
        fm.setDeletedAt(null);

        fm = fileMetadataRepository.saveAndFlush(fm);

        return toFileResponse(fm);
    }

    @Transactional
    public FileResponse updateFileMetadata(UUID userId, UUID fileId, PatchFileRequest request){

        if(request.fileName() == null && request.aiDescription() == null){
            throw new InvalidFileRequestException("At least one field must be provided.");
        }

        FileMetadata fm = fileMetadataRepository.findByIdAndUserIdAndDeletedFalse(fileId, userId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        if(request.fileName() != null){
            validateFileName(request.fileName());
            fm.setFileName(request.fileName());
        }

        if(request.aiDescription() != null){
            if(request.aiDescription().length() > 500){
                throw new InvalidFileRequestException("aiDescription must not exceed 500 characters.");
            }
            fm.setAiDescription(request.aiDescription());
        }

        fm = fileMetadataRepository.saveAndFlush(fm);

        return toFileResponse(fm);
    }

    @Transactional
    public VersionRestoreResponse restoreToVersion(UUID userId, UUID fileId, Integer versionNumber){

        FileMetadata fm = fileMetadataRepository.findByIdAndUserIdAndDeletedFalse(fileId, userId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        if(versionNumber.equals(fm.getVersionNumber())){
            throw new InvalidFileRequestException("File is already at version " + versionNumber + ".");
        }

        FileVersion targetVersion = fileVersionRepository.findByFileIdAndVersionNumber(fileId, versionNumber)
                .orElseThrow(() -> new FileNotFoundException("Version " + versionNumber + " not found for this file."));

        int newVersionNumber = fm.getVersionNumber() + 1;

        FileVersion restoredVersion = FileVersion.builder()
                .fileId(fileId)
                .userId(userId)
                .createdBy(userId)
                .versionNumber(newVersionNumber)
                .s3Key(targetVersion.getS3Key())
                .fileName(targetVersion.getFileName())
                .fileSize(targetVersion.getFileSize())
                .build();
        fileVersionRepository.save(restoredVersion);

        fm.setFileName(targetVersion.getFileName());
        fm.setFileSizeBytes(targetVersion.getFileSize());
        fm.setS3Key(targetVersion.getS3Key());
        fm.setVersionNumber(newVersionNumber);
        fm.setAiSummary(null);
        fm.setAiDescription(null);
        fm.setAiTags(null);
        fm.setAiProcessedAt(null);

        fileMetadataRepository.save(fm);

        return new VersionRestoreResponse(
                fileId,
                fm.getFileName(),
                fm.getFileSizeBytes(),
                versionNumber,
                newVersionNumber,
                "File restored to version " + versionNumber + " successfully.");
    }



    private FileResponse toFileResponse(FileMetadata fm){
        return new FileResponse(
                fm.getId(),
                fm.getFileName(),
                fm.getFileSizeBytes(),
                fm.getMimeType(),
                fm.getVersionNumber(),
                fm.getS3Key(),
                fm.getDeleted(),
                fm.getConflictCopy(),
                fm.getOriginalFileId(),
                fm.getAiSummary(),
                fm.getAiDescription(),
                fm.getAiTags(),
                fm.getAiProcessedAt(),
                fm.getCreatedAt(),
                fm.getUpdatedAt());
    }

    private void validateFileName(String fileName){
        if(fileName.isBlank()){
            throw new InvalidFileRequestException("fileName must not be blank.");
        }
        if(fileName.length() > 255){
            throw new InvalidFileRequestException("fileName must not exceed 255 characters.");
        }
        if(fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")){
            throw new InvalidFileRequestException("fileName must not contain path traversal characters.");
        }
    }
}
