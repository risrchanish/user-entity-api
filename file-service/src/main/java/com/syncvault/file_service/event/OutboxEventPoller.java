package com.syncvault.file_service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syncvault.file_service.entity.OutboxEvent;
import com.syncvault.file_service.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OutboxEventPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final FileEventProducer fileEventProducer;
    private final ObjectMapper objectMapper;

    public OutboxEventPoller(OutboxEventRepository outboxEventRepository,
                             FileEventProducer fileEventProducer,
                             ObjectMapper objectMapper){
        this.outboxEventRepository = outboxEventRepository;
        this.fileEventProducer = fileEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollAndPublish(){

        List<OutboxEvent> pending = outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc();

        for(OutboxEvent outboxEvent : pending){
            try{
                FileEvent event = objectMapper.readValue(outboxEvent.getPayload(), FileEvent.class);

                fileEventProducer.send(outboxEvent.getTopic(), event).get();

                outboxEvent.setPublished(true);
                outboxEvent.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(outboxEvent);
            }catch(Exception ex){
                log.error("Failed to publish outbox event {} -- will retry on next poll", outboxEvent.getId(), ex);
            }
        }
    }
}