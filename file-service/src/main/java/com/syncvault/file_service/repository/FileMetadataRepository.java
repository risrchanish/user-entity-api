package com.syncvault.file_service.repository;

import com.syncvault.file_service.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    @Query("SELECT SUM(f.fileSizeBytes) FROM FileMetadata f WHERE f.userId = :userId AND f.deleted = false")
    Long sumFileSizeByUserId(@Param("userId") UUID userId);

    Optional<FileMetadata> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);

    List<FileMetadata> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Optional<FileMetadata> findByIdAndUserId(UUID id, UUID userId);
}
