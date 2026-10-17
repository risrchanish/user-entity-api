package com.syncvault.file_service.utility;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(

        String endPoint,
        String region,
        String bucketName,
        String accessKey,
        String secretKey,
        Integer presignedUrlExpiryMinutes
) {}
