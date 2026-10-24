package com.syncvault.file_service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syncvault.file_service.entity.OutboxEvent;
import com.syncvault.file_service.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OutboxEventWriter {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventWriter(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper){
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void writeUploadedEvent(UUID fileId, UUID userId, String fileName, long fileSize, int versionNumber){
        write(FileEventProducer.TOPIC_UPLOADED, "FILE_UPLOADED", fileId, userId, fileName, fileSize, versionNumber);
    }

    public void writeUpdatedEvent(UUID fileId, UUID userId, String fileName, long fileSize, int versionNumber){
        write(FileEventProducer.TOPIC_UPDATED, "FILE_UPDATED", fileId, userId, fileName, fileSize, versionNumber);
    }

    public void writeDeletedEvent(UUID fileId, UUID userId, String fileName, long fileSize, int versionNumber){
        write(FileEventProducer.TOPIC_DELETED, "FILE_DELETED", fileId, userId, fileName, fileSize, versionNumber);
    }

    public void writeConflictEvent(UUID fileId, UUID userId, String fileName, long fileSize, int versionNumber){
        write(FileEventProducer.TOPIC_CONFLICT, "FILE_CONFLICT", fileId, userId, fileName, fileSize, versionNumber);
    }

    private void write(String topic, String eventType, UUID fileId, UUID userId, String fileName,
                        long fileSize, int versionNumber){

        FileEvent event = FileEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .userId(userId)
                .fileId(fileId)
                .fileName(fileName)
                .fileSize(fileSize)
                .versionNumber(versionNumber)
                .timestamp(Instant.now())
                .build();

        String payload;
        try{
            payload = objectMapper.writeValueAsString(event);
        }catch(JsonProcessingException ex){
            throw new UncheckedIOException(ex);
        }

        outboxEventRepository.save(OutboxEvent.builder()
                .id(event.getEventId())
                .topic(topic)
                .eventKey(fileId.toString())
                .payload(payload)
                .published(false)
                .createdAt(LocalDateTime.now())
                .build());
    }
}