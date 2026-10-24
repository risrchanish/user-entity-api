package com.syncvault.file_service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class FileEventProducer {

    public static final String TOPIC_UPLOADED = "file.uploaded";
    public static final String TOPIC_UPDATED = "file.updated";
    public static final String TOPIC_DELETED = "file.deleted";
    public static final String TOPIC_CONFLICT = "file.conflict";

    private final KafkaTemplate<String, FileEvent> kafkaTemplate;

    public FileEventProducer(KafkaTemplate<String, FileEvent> kafkaTemplate)
    {
        this.kafkaTemplate = kafkaTemplate;
    }

    CompletableFuture<SendResult<String, FileEvent>> send(String topic, FileEvent event){

        return kafkaTemplate.send(topic, event.getFileId().toString(), event)
                .whenComplete((result, ex) -> {
                    if(ex == null){
                        log.info("Published event {} to topic={} partition={} offset={}",
                                event.getEventId(),
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }else{
                        log.error("Failed to publish event {} fileId={} topic={}",
                                event.getEventId(), event.getFileId(), topic, ex);
                    }
                });
    }
}
