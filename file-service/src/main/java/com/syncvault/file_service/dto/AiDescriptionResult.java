package com.syncvault.file_service.dto;

public record AiDescriptionResult(String summary, String description, String tags) {

    public static AiDescriptionResult parse(String raw) {
        String summary = extractField(raw, "SUMMARY");
        String description = extractField(raw, "DESCRIPTION");
        String tags = extractField(raw, "TAGS");
        return new AiDescriptionResult(summary, description, tags);
    }

    private static String extractField(String raw, String label) {
        for (String line : raw.lines().toList()) {
            if (line.trim().startsWith(label + ":")) {
                return line.trim().substring((label + ":").length()).trim();
            }
        }
        return "";
    }
}