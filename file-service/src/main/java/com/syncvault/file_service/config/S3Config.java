package com.syncvault.file_service.config;

import com.syncvault.file_service.utility.S3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    private final S3Properties s3Properties;

    public S3Config(S3Properties s3Properties){
        this.s3Properties = s3Properties;
    }

    @Bean
    public S3Client s3Client(){

        S3ClientBuilder builder = S3Client.builder().region(Region.of(s3Properties.region()));

        if(s3Properties.endPoint() != null && !s3Properties.endPoint().isBlank()){
            builder.endpointOverride(URI.create(s3Properties.endPoint()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(s3Properties.accessKey(),s3Properties.secretKey())))
                    .forcePathStyle(true);
        }
        S3Client client = builder.build();
        ensureBucketExists(client);
        return client;
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder().region(Region.of(s3Properties.region()));

        if (s3Properties.endPoint() != null && !s3Properties.endPoint().isBlank()) {
            builder.endpointOverride(URI.create(s3Properties.endPoint()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(s3Properties.accessKey(), s3Properties.secretKey())))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return builder.build();
    }

    private void ensureBucketExists(S3Client client){
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(s3Properties.bucketName()).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                client.createBucket(CreateBucketRequest.builder().bucket(s3Properties.bucketName()).build());
            } else {
                throw e;
            }
        }
    }
}
