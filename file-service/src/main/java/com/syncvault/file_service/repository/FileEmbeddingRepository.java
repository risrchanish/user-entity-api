package com.syncvault.file_service.repository;

import com.syncvault.file_service.entity.FileEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileEmbeddingRepository extends JpaRepository<FileEmbedding, UUID> {
}
