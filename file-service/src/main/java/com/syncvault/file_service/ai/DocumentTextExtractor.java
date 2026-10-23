package com.syncvault.file_service.ai;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Slf4j
public class DocumentTextExtractor {

    public String extractText(InputStream fileStream, String contentType) {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        if (contentType != null) {
            metadata.set(Metadata.CONTENT_TYPE, contentType);
        }

        try {
            parser.parse(fileStream, handler, metadata);
            return handler.toString();
        } catch (Exception ex) {
            log.warn("Text extraction failed for contentType={}: {}", contentType, ex.getMessage());
            return "";
        }
    }
}