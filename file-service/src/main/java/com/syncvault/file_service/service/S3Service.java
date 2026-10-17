package com.syncvault.file_service.service;

import com.syncvault.file_service.utility.S3Properties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final int presignedUrlExpiryMinutes;


    public S3Service(S3Client s3Client, S3Presigner s3Presigner, S3Properties s3Properties){
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = s3Properties.bucketName();
        this.presignedUrlExpiryMinutes = s3Properties.presignedUrlExpiryMinutes();
    }

    public void uploadFile(String s3Key, byte[] bytes, String contentType){
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType(contentType)
                        .build(), RequestBody.fromBytes(bytes));
    }

    public String buildS3Key(UUID userId, UUID fileId, int versionNumber, String fileName){
        return userId + "/"  + fileId + "/" + versionNumber + "/" +fileName;
    }

    public String generatePresignedDownloadUrl(String s3Key){
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    public int getPresignedUrlExpiryMinutes(){
        return presignedUrlExpiryMinutes;
    }
}
