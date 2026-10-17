package com.syncvault.file_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_versions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
