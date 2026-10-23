package com.syncvault.file_service.ai;

import com.syncvault.aisdk.LLMClient;
import com.syncvault.aisdk.model.EmbeddingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
public class EmbeddingService {

    private final JdbcTemplate jdbcTemplate;
    private final LLMClient llmClient;

    public EmbeddingService(JdbcTemplate jdbcTemplate, LLMClient llmClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.llmClient = llmClient;
    }

    public void generateAndStore(UUID fileId, UUID userId, String extractedText) {
        if (extractedText == null || extractedText.isBlank()) {
            log.info("No extractable text for file {} — skipping embedding.", fileId);
            return;
        }

        EmbeddingResponse response;
        try {
            response = llmClient.embed(extractedText);
        } catch (Exception ex) {
            log.warn("Embedding generation failed for file {}: {}", fileId, ex.getMessage());
            return;
        }

        String vectorLiteral = toVectorLiteral(response.embedding());

        jdbcTemplate.update(
                "INSERT INTO file_embeddings (id, file_id, user_id, embedding, created_at) " +
                        "VALUES (?, ?, ?, CAST(? AS vector), NOW())",
                UUID.randomUUID(), fileId, userId, vectorLiteral);
    }

    private static String toVectorLiteral(float[] embedding) {
        return "[" + IntStream.range(0, embedding.length)
                .mapToObj(i -> String.valueOf(embedding[i]))
                .collect(Collectors.joining(",")) + "]";
    }
}