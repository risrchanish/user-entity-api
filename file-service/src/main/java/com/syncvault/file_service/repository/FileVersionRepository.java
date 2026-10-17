package com.syncvault.file_service.repository;

import com.syncvault.file_service.entity.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileVersionRepository extends JpaRepository<FileVersion, UUID> {

    List<FileVersion> findByFileIdOrderByVersionNumberAsc(UUID fileId);

    Optional<FileVersion> findByFileIdAndVersionNumber(UUID fileId, Integer versionNumber);
}
