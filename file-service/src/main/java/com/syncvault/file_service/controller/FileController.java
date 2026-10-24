package com.syncvault.file_service.controller;

import com.syncvault.file_service.ai.FileAiCoordinator;
import com.syncvault.file_service.ai.FileSearchService;
import com.syncvault.file_service.dto.*;
import com.syncvault.file_service.event.FileEventProducer;
import com.syncvault.file_service.exception.ErrorResponse;
import com.syncvault.file_service.exception.FileConflictException;
import com.syncvault.file_service.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.syncvault.file_service.constant.CommonConstant.HEADER_USER_ID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final FileAiCoordinator fileAiCoordinator;
    private final FileSearchService fileSearchService;

    public FileController(FileService fileService, FileAiCoordinator fileAiCoordinator,
                          FileSearchService fileSearchService){
        this.fileService = fileService;
        this.fileAiCoordinator = fileAiCoordinator;
        this.fileSearchService = fileSearchService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFile(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @RequestParam("file") MultipartFile file){

        if(file.isEmpty()){
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("File must not be empty."));
        }

        FileUploadResponse response = fileService.uploadFile(userId, file);

        fileAiCoordinator.processAsync(response.fileId(), userId, response.s3Key(), response.contentType());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PutMapping(value = "/{fileId}/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> reUploadFile(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @PathVariable("fileId") UUID fileId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expectedVersion", required = false) Integer expectedVersion){

        if(file.isEmpty()){
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("File must not be empty."));
        }

        FileUpdateResponse response = fileService.reUploadFile(userId, fileId, file, expectedVersion);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/versions")
    public ResponseEntity<List<FileVersionResponse>> getFileVersions(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @PathVariable("fileId") UUID fileId){

        return ResponseEntity.ok(fileService.getFileVersions(userId, fileId));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFile(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @PathVariable("fileId") UUID fileId){
        return ResponseEntity.ok(fileService.getFile(userId, fileId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> listUserFiles(
            @RequestHeader(HEADER_USER_ID) UUID authenticatedUserId,
            @PathVariable("userId") UUID userId){
        return ResponseEntity.ok(fileService.listUserFiles(authenticatedUserId, userId));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> getDownloadUrl(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @PathVariable("fileId") UUID fileId){
        return ResponseEntity.ok(fileService.getDownloadUrl(userId, fileId));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @PathVariable("fileId") UUID fileId){
       fileService.deleteFile(userId, fileId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{fileId}/restore")
    public ResponseEntity<FileResponse> restoreFile(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @PathVariable("fileId") UUID fileId){
        return ResponseEntity.ok(fileService.restoreFile(userId, fileId));
    }

    @PatchMapping("/{fileId}")
    public ResponseEntity<FileResponse> updateFile(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @PathVariable("fileId") UUID fileId,
            @RequestBody PatchFileRequest request){
        return ResponseEntity.ok(fileService.updateFileMetadata(userId, fileId, request));
    }

    @PostMapping("/{fileId}/versions/{versionNumber}/restore")
    public ResponseEntity<VersionRestoreResponse> restoreVersion(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @PathVariable("fileId") UUID fileId,
            @PathVariable("versionNumber") Integer versionNumber){

        VersionRestoreResponse response = fileService.restoreToVersion(userId, fileId, versionNumber);
        fileAiCoordinator.processAsync(response.fileId(), userId, response.s3Key(), response.contentType());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFiles(
            @RequestHeader(HEADER_USER_ID) UUID userId,
            @RequestParam(value = "q", required = false) String q){

        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Search query must not be blank."));
        }

        return ResponseEntity.ok(fileSearchService.search(userId, q));
    }
}
