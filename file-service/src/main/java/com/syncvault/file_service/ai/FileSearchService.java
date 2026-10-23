package com.syncvault.file_service.ai;

import com.syncvault.aisdk.LLMClient;
import com.syncvault.aisdk.model.EmbeddingResponse;
import com.syncvault.file_service.dto.FileSearchResult;
import com.syncvault.file_service.exception.SearchUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
public class FileSearchService {

    private static final int DEFAULT_LIMIT = 10; // not specified by the story — my own default

    private static final String SEARCH_SQL = """
            SELECT f.id, f.file_name, f.ai_summary, f.ai_tags,
                   1 - (fe.embedding <=> CAST(:embedding AS vector)) AS relevance_score
            FROM file_embeddings fe
            JOIN files f ON fe.file_id = f.id
            WHERE fe.user_id = :userId AND f.deleted = false
            ORDER BY fe.embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LLMClient llmClient;

    public FileSearchService(NamedParameterJdbcTemplate jdbcTemplate, LLMClient llmClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.llmClient = llmClient;
    }

    public List<FileSearchResult> search(UUID userId, String query) {
        EmbeddingResponse response;
        try {
            response = llmClient.embed(query); // AC-020-05 step 1
        } catch (Exception ex) {
            log.warn("Query embedding failed for user {}: {}", userId, ex.getMessage());
            throw new SearchUnavailableException("Search service temporarily unavailable. Please try again later."); // AC-020-09
        }

        String vectorLiteral = toVectorLiteral(response.embedding());

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("embedding", vectorLiteral)
                .addValue("userId", userId)
                .addValue("limit", DEFAULT_LIMIT);

        return jdbcTemplate.query(SEARCH_SQL, params, (rs, rowNum) -> new FileSearchResult(
                rs.getObject("id", UUID.class),
                rs.getString("file_name"),
                rs.getString("ai_summary"),
                rs.getString("ai_tags"),
                rs.getDouble("relevance_score")
        ));
    }

    private static String toVectorLiteral(float[] embedding) {
        return "[" + IntStream.range(0, embedding.length)
                .mapToObj(i -> String.valueOf(embedding[i]))
                .collect(Collectors.joining(",")) + "]";
    }
}